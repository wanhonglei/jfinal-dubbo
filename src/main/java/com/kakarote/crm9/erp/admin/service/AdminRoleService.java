package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.cache.CaffeineCache;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminMenu;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUserRole;
import com.kakarote.crm9.utils.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色服务类
 * @author honglei.wan
 */
public class AdminRoleService {
    @Inject
    private AdminMenuService adminMenuService;

    @Inject
    private AdminUserService adminUserService;

    /**
     * @author wyq
     * 获取全部角色列表
     */
    public List<Record> getAllRoleList() {
        List<Record> records = new ArrayList<>(BaseConstant.ROLE_TYPES.length);
        Record menuRecord = Db.findFirst(Db.getSql("admin.menu.queryMenuByRealm"),"board");
        for (Integer roleType : BaseConstant.ROLE_TYPES) {
            Record record = new Record();
            record.set("name", roleTypeCaseName(roleType));
            record.set("pid", roleType);
            List<Record> recordList = Db.find(Db.getSql("admin.role.getRoleListByRoleType"), roleType);
            recordList.forEach(role -> {
                List<Integer> crm = Db.query(Db.getSql("admin.role.getRoleMenu"), role.getInt("id"), 1, 1);
                List<Integer> bi = Db.query(Db.getSql("admin.role.getRoleMenu"), role.getInt("id"), 2, 2);
                List<Integer> board = Db.query(Db.getSql("admin.role.getRoleMenu"), role.getInt("id"),menuRecord == null ? null : menuRecord.get("menu_id") , menuRecord == null ? null : menuRecord.get("menu_id"));
                role.set("rules",new JSONObject().fluentPut("crm",crm).fluentPut("bi",bi).fluentPut("board",board));
            });
            record.set("list", recordList);
            records.add(record);
        }
        return records;
    }

    /**
     * @author wyq
     * 新建
     */
    public R save(AdminRole adminRole) {
        Integer number = Db.queryInt("select count(*) from 72crm_admin_role where role_name = ? and role_type = ?", adminRole.getRoleName(), adminRole.getRoleType());
        if (number > 0) {
            return R.error("角色名已存在");
        }
        return adminRole.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 编辑角色
     */
    @Before(Tx.class)
    public Integer update(AdminRole adminRole) {
        adminRole.update();
        List<Integer> menuList;
        if (adminRole.getMenuIds() != null) {
            try {
                menuList = JSON.parseArray(URLDecoder.decode(adminRole.getMenuIds(), StandardCharsets.UTF_8.name()), Integer.class);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("数据错误", e);
            }
            adminMenuService.saveRoleMenu(adminRole.getRoleId().intValue(), adminRole.getDataType(), menuList);
            return 1;
        }
        return 0;
    }

    @Before(Tx.class)
    public void updateRoleMenu(JSONObject jsonObject) {
        adminMenuService.saveRoleMenu(jsonObject.getInteger("id"), jsonObject.getInteger("type"), jsonObject.getJSONArray("rules").toJavaList(Integer.class));
    }

    /**
     * 查看权限
     */
    public JSONObject auth(Long userId) {
        //如果用户id为null，直接返回空的json对象
        if (userId == null){
            return new JSONObject();
        }
        JSONObject jsonObject=CaffeineCache.ME.get("role:permissions",userId.toString());
        if(jsonObject!=null){
            return jsonObject;
        }
        jsonObject = new JSONObject();
        List<Record> menuRecords;
        List<Integer> roleIds = queryRoleIdsByUserId(userId);
        if (roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
            menuRecords = adminMenuService.queryAllMenu();
        } else {
            menuRecords = adminMenuService.queryMenuByUserId(userId);
        }
        List<AdminMenu> adminMenus = adminMenuService.queryMenuByParentId(0);
        for (AdminMenu adminMenu : adminMenus) {
            JSONObject object = new JSONObject();
            List<AdminMenu> adminMenuList = adminMenuService.queryMenuByParentId(adminMenu.getMenuId().intValue());
            for (AdminMenu menu : adminMenuList) {
                JSONObject authObject = new JSONObject();
                for (Record record : menuRecords) {
                    if (menu.getMenuId().equals(record.getLong("parent_id"))) {
                        authObject.put(record.getStr("realm"), Boolean.TRUE);
                    }
                }
                if (!authObject.isEmpty()) {
                    object.put(menu.getRealm(), authObject);
                }
            }
            if (adminMenu.getMenuId() == 3L) {
                if (roleIds.contains(2) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("system", Boolean.TRUE);
                    object.put("user", Boolean.TRUE);
                    object.put("examineFlow", Boolean.TRUE);
                    object.put("oa", Boolean.TRUE);
                    object.put("crm", Boolean.TRUE);
                    object.put("permission", Boolean.TRUE);
                }
                if (roleIds.contains(3) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("user", Boolean.TRUE);
                }
                if (roleIds.contains(4) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("examineFlow", Boolean.TRUE);
                }
                if (roleIds.contains(5) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("oa", Boolean.TRUE);
                }
                if (roleIds.contains(6) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("crm", Boolean.TRUE);
                }
            }
            if (!object.isEmpty()) {
                jsonObject.put(adminMenu.getRealm(), object);
            }
        }
        CaffeineCache.ME.put("role:permissions:"+ userId,jsonObject);
        return jsonObject;
    }

    /**
     * @author wyq
     * 删除
     */
    public boolean delete(Integer roleId) {
        Record record = Db.findFirst("select count(*) as menuNum from 72crm_admin_role_menu where role_id = ?", roleId);
        if (record.getInt("menuNum") == 0) {
            return Db.delete(Db.getSql("admin.role.deleteRole"), roleId) > 0;
        }
        return Db.tx(() -> {
            Db.delete(Db.getSql("admin.role.deleteRole"), roleId);
            Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);
            return true;
        });
    }

    /**
     * @author wyq
     * 复制
     */
    @Before(Tx.class)
    public void copy(Integer roleId) {
        AdminRole adminRole = AdminRole.dao.findById(roleId);
        List<Record> recordList = Db.find(Db.getSql("admin.role.getMenuIdsList"), roleId);
        List<Integer> menuIdsList = new ArrayList<>(recordList.size());
        for (Record record : recordList) {
            menuIdsList.add(record.getInt("menu_id"));
        }
        String roleName = adminRole.getRoleName().trim();
        String pre = ReUtil.delFirst("[(]\\d+[)]$", roleName);
        List<AdminRole> adminRoleList;
        if (!ReUtil.contains("^[(]\\d+[)]$", roleName)) {
            adminRoleList = AdminRole.dao.find("select * from 72crm_admin_role where role_name like '" + pre + "%'");
        } else {
            adminRoleList = AdminRole.dao.find("select * from 72crm_admin_role where role_name regexp '^[(]\\d+[)]$'");
        }
        StringBuilder numberSb = new StringBuilder();
        for (AdminRole dbAdminRole : adminRoleList) {
            String endCode = ReUtil.get("[(]\\d+[)]$", dbAdminRole.getRoleName(), 0);
            if (endCode != null) {
                numberSb.append(endCode);
            }
        }
        int i = 1;
        if (numberSb.length() == 0) {
            while (numberSb.toString().contains("(" + i + ")")) {
                i++;
            }
        }
        adminRole.setRoleName(pre + "(" + i + ")");
        adminRole.setRoleId(null);
        adminRole.save();
        Integer copyRoleId = adminRole.getInt("role_id");
        adminMenuService.saveRoleMenu(copyRoleId, adminRole.getDataType(), menuIdsList);
    }

    /**
     * @author wyq
     * 角色关联员工
     */
    @Before(Tx.class)
    public R relatedUser(AdminUserRole adminUserRole) {
        if (adminUserRole != null && adminUserRole.getUserIds() != null) {
            String[] userIdsArr = adminUserRole.getUserIds().split(",");
            String[] roleIdsArr = adminUserRole.getRoleIds().split(",");
            AdminUserRole userRole = new AdminUserRole();
            for (String userId : userIdsArr) {
                for (String roleId : roleIdsArr) {
                    Db.delete("delete from 72crm_admin_user_role where user_id = ? and role_id = ?", Integer.valueOf(userId), Integer.valueOf(roleId));
                    userRole.clear();
                    userRole.setUserId(Long.valueOf(userId));
                    userRole.setRoleId(Integer.valueOf(roleId));
                    userRole.save();

                    adminUserService.clearUserCache(Long.valueOf(userId));
                }
            }
            return R.ok();
        } else {
            return R.error("请选择角色和员工");
        }
    }

    /**
     * @author wyq
     * 解除角色关联员工
     */
    public R unbindingUser(AdminUserRole adminUserRole) {
        if (adminUserRole.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
            return R.error("超级管理员不可被更改");
        }
        return Db.delete("delete from 72crm_admin_user_role where user_id = ? and role_id = ?", adminUserRole.getUserId(), adminUserRole.getRoleId()) > 0 ? R.ok() : R.error();
    }

    public List<Integer> queryRoleIdsByUserId(Long userId) {
        return Db.query(Db.getSql("admin.role.queryRoleIdsByUserId"), userId);
    }

    /**
     * 角色类型转换名称
     *
     * @param type 类型
     * @return 角色名称
     */
    private String roleTypeCaseName(Integer type) {
        String name;
        switch (type) {
            case 1:
                name = "管理角色";
                break;
            case 2:
                name = "客户管理角色";
                break;
            case 3:
                name = "人事角色";
                break;
            case 4:
                name = "财务角色";
                break;
            case 5:
                name = "项目角色";
                break;
            default:
                name = "自定义角色";
        }
        return name;
    }

    /**
     * Get role by user id.
     *
     * @param userId crm user id
     * @return {@code AdminRole}
     */
    public List<AdminRole> getRoleByUserId(Integer userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        List<Record> records = Db.find(Db.getSql("admin.role.getRoleByUserId"), userId);
        if (Objects.nonNull(records)) {
            return records.stream().map(item -> new AdminRole()._setAttrs(item.getColumns())).collect(Collectors.toList());
        } else {
            return null;
        }

    }
}
