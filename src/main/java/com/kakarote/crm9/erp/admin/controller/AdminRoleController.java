package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.AdminUserRole;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.qxwz.buc.sso.client.web.BucWebUser;
import com.qxwz.buc.sso.client.web.BucWebUserUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AdminRoleController extends Controller {
    @Inject
    private AdminRoleService adminRoleService;

    /**
     * @author wyq
     * 获取全部角色列表
     */
    public void getAllRoleList(){
        renderJson(R.ok().put("data",adminRoleService.getAllRoleList()));
    }

    /**
     * @author wyq
     * 新建
     */
    @Before(Tx.class)
    public void add(@Para("")AdminRole adminRole){
        renderJson(adminRoleService.save(adminRole));
    }

    /**
     * @author wyq
     * 编辑角色
     */
    @NotNullValidate(value = "roleId",message = "角色id不能为空")
    @NotNullValidate(value = "roleName",message = "角色名称不能为空")
    public void update(@Para("")AdminRole adminRole){
        Integer number = Db.queryInt("select count(*) from 72crm_admin_role where role_name = ? and role_type = ? and role_id != ?", adminRole.getRoleName(),adminRole.getRoleType(),adminRole.getRoleId());
        if (number > 0){
            renderJson(R.error("角色名已存在"));
        }else {
            renderJson(R.ok().put("data",adminRoleService.update(adminRole)));
        }
    }

    /**
     * 修改角色菜单
     * @author zhangzhiwei
     */
    public void updateRoleMenu(){
        adminRoleService.updateRoleMenu(JSON.parseObject(getRawData()));
        renderJson(R.ok());
    }

    /**
     * 查看当前登录人的权限
     * @author zhangzhiwei
     */
    public void auth(){
        AdminUser user = BaseUtil.getUser();
        if(user == null || StringUtils.isEmpty(user.getEmail())) {
            BucWebUser bucUser = BucWebUserUtil.retrieveWebUser(getRequest());
            String username = bucUser.getEmail().substring(0, bucUser.getEmail().indexOf('@'));
            user = AdminUser.dao.findFirst(Db.getSql("admin.user.queryByUserName"), username);

            if (user == null) {
                renderJson(R.error(HttpStatus.SC_UNAUTHORIZED,"user does not exist!"));
                return;
            } else {
                user.setLastLoginIp(BaseUtil.getLoginAddress(getRequest()));
                user.setLastLoginTime(new Date());
                user.update();
                user.setRoles(adminRoleService.queryRoleIdsByUserId(user.getUserId()));

                // set crm user to redis
                CrmUser crmUser = BaseUtil.getCrmUser();
                if (Objects.isNull(crmUser)) {
                    List<AdminRole> roles = adminRoleService.getRoleByUserId(user.getUserId().intValue());
                    crmUser = new CrmUser(user, roles);
                    Redis.use().setex(BaseUtil.getToken(getRequest()), 360000, crmUser);
                }
            }
        }
        renderJson(R.ok().put("data",adminRoleService.auth(user.getUserId())));
    }
    /**
     * @author wyq
     * @param roleId 角色id
     * 复制
     */
    public void copy(@Para("roleId")Integer roleId){
        adminRoleService.copy(roleId);
        renderJson(R.ok());
    }

    /**
     * @author wyq
     * @param roleId 角色id
     * 删除
     */
    public void delete(@Para("roleId")Integer roleId){
        renderJson(adminRoleService.delete(roleId) ? R.ok() : R.error());
    }

    /**
     * @author wyq
     * 关联员工
     */
    public void relatedUser(@Para("")AdminUserRole adminUserRole){
        renderJson(adminRoleService.relatedUser(adminUserRole));
    }

    /**
     * @author wyq
     * 解除角色关联员工
     */
    public void unbindingUser(@Para("") AdminUserRole adminUserRole){
        renderJson(adminRoleService.unbindingUser(adminUserRole));
    }
}
