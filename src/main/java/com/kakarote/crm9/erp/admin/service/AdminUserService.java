package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.AdminUserRole;
import com.kakarote.crm9.erp.admin.vo.DataPermissionVO;
import com.kakarote.crm9.erp.crm.acl.dataauth.CrmDataAuthEnum;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.common.scene.CrmCustomerSceneEnum;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.erp.crm.service.CrmChangeLogService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.Sort;
import com.kakarote.crm9.utils.TagUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminUserService class
 *
 * @author yue.li
 * @date 2019/11/27
 */
public class AdminUserService {
    @Inject
    private AdminRoleService adminRoleService;
    @Inject
    private AdminDeptService adminDeptService;
    @Inject
    private CrmCustomerService crmCustomerService;
    @Inject
    private CrmBusinessService crmBusinessService;
    @Inject
    private CrmChangeLogService crmChangeLogService;

    public static String MY_DEPT_AND_SUB = "CRM:myDeptAndSub:";
    public static String MY_SELF_AND_SUB = "CRM:myselfAndSub:";
    public static String MY_DEPT = "CRM:myDept:";

    /**
     * 校验用户是否保存 用户下是否存在绑定的客户和商机
     *
     * @param adminUser
     * @return
     */
    public R checkUserCustomerAndBusiness(AdminUser adminUser) {
        if (Objects.isNull(adminUser.getUserId())) {
            return R.error(CrmErrorInfo.USER_USERID_IS_EMPTY);
        }
        Record record = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), adminUser.getUserId());
        if (Objects.isNull(record)) {
            return R.error(CrmErrorInfo.USER_QUERY_USER_INFO_EMPTY);
        }
        Integer oldDeptId = record.getInt("dept_id");

        //判断用户是否可以设置直属上级
        if (Objects.nonNull(adminUser.getParentId()) && adminUser.getParentId() != 0) {
            boolean checkEnableSetUserParentId = checkSetUserParentId(adminUser.getUserId(), adminUser.getParentId());
            if (!checkEnableSetUserParentId) {
                return R.error(CrmErrorInfo.USER_SET_PARENT_IS_ERROR);
            }
        }
        //如果部门没有变动，返回可以保存
        if (Objects.equals(oldDeptId, adminUser.getDeptId())) {
            return R.ok();
        }
        String oldBusinessDepartmentId = "";
        //查询当前用户的历史事业部和现在的事业部
        if (Objects.nonNull(oldDeptId)) {
            oldBusinessDepartmentId = adminDeptService.getBusinessDepartmentByDeptIdNew(oldDeptId.toString());
        }
        String nowBusinessDepartmentId = adminDeptService.getBusinessDepartmentByDeptIdNew(String.valueOf(adminUser.getDeptId()));
        //查询用户负责的客户数量和负责的商机数量
        Integer customerCount = crmCustomerService.countCustomerListByOwnerUserId(adminUser.getUserId());
        Integer businessCount = crmBusinessService.countBusinessListByOwnerUserId(adminUser.getUserId());
        boolean deptChange = false;
        if (StringUtils.isNotBlank(oldBusinessDepartmentId) && !Objects.equals(oldBusinessDepartmentId, nowBusinessDepartmentId)) {
            deptChange = true;
        }
        boolean customerRelease = (Objects.nonNull(customerCount) && customerCount == 0
                && Objects.nonNull(businessCount) && businessCount == 0);
        String message = getMesaage(deptChange, customerCount, businessCount);

        //可以保存判断：1.用户所在事业部没有变动 2. 用户所在事业部变动了，但是负责的客户和商机都释放了
        boolean enableSave = !deptChange || customerRelease;
        return enableSave ? R.ok(message) : R.error(message);
    }

    private String getMesaage(boolean deptChange, int customerCount, int businessCount) {
        if (customerCount <= 0 && businessCount <= 0) {
            return null;
        }
        if (deptChange) {
            return "该员工进行跨事业部调整，名下存在" + customerCount + "个客户，" + businessCount
                    + "个商机，请将客户/商机资源全部转出后，方可进行跨事业部调整";
        } else {
            return "该员工在本事业部调整，名下存在" + customerCount + "个客户，"
                    + businessCount + "个商机，客户/商机将留在该员工名下";
        }
    }

    /**
     * 编辑用户 包含性别，部门，直属上级，角色
     *
     * @param adminUser
     * @param roleIds
     * @return
     */
    @Before(Tx.class)
    public R setUser(AdminUser adminUser, String roleIds) {
        Long userId = adminUser.getUserId();
        if (Objects.isNull(userId)) {
            return R.error(CrmErrorInfo.USER_USERID_IS_EMPTY);
        }
        Record record = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), userId);
        if (Objects.isNull(record)) {
            return R.error(CrmErrorInfo.USER_QUERY_USER_INFO_EMPTY);
        }
        R result = checkUserCustomerAndBusiness(adminUser);
        if (!result.isSuccess()) {
            return result;
        }
        AdminUser userInDb = new AdminUser()._setAttrs(record.getColumns());
        Integer oldDeptId = userInDb.getDeptId();
        Integer newDeptId = adminUser.getDeptId();
        //编辑用户信息
        userInDb.setSex(adminUser.getSex());
        userInDb.setDeptId(adminUser.getDeptId());
        userInDb.setParentId(adminUser.getParentId());
        boolean updateUser = userInDb.update();

        Db.delete("delete from 72crm_admin_user_role where user_id = ?", userId);
        Db.delete("delete from 72crm_admin_scene where user_id = ? and is_system = 1", userId);

        //用户部门发生了变化，记录变化日志
        if (!Objects.equals(oldDeptId, newDeptId)) {
            crmChangeLogService.saveBdDeptChangeLog(userId, Long.valueOf(newDeptId), BaseUtil.getUserId());
        }

        // 更新角色信息
        if (StrUtil.isNotBlank(roleIds)) {
            for (Integer roleId : TagUtil.toSet(roleIds)) {
                AdminUserRole adminUserRole = new AdminUserRole();
                adminUserRole.setUserId(userId);
                adminUserRole.setRoleId(roleId);
                adminUserRole.save();
            }
        }

        // 清空用户历史缓存
        clearUserCache(userId);
        return R.isSuccess(updateUser);
    }

    /**
     * 清除用户的缓存
     * @param userId
     */
    public void clearUserCache(Long userId){
        Redis.use().del(MY_DEPT_AND_SUB + userId);
        Redis.use().del(MY_SELF_AND_SUB + userId);
        Redis.use().del(MY_DEPT + userId);
    }

    /**
     * 判断是否可以设置用户上级
     *
     * @param userId       用户ID
     * @param userParentId 用户上级ID
     * @return
     */
    private boolean checkSetUserParentId(Long userId, Long userParentId) {
        if (Objects.nonNull(userId) && Objects.nonNull(userParentId)) {
            List<Record> topUserList = queryTopUserList(userId);
            if (CollectionUtils.isNotEmpty(topUserList)) {
                List<Long> enableParentIds = topUserList.stream().map(p -> p.getLong("user_id"))
                        .collect(Collectors.toList());
                return CollectionUtils.isNotEmpty(enableParentIds) && enableParentIds.contains(userParentId);
            }
        }
        return false;
    }

    /**
     * 重置用户信息
     *
     * @param adminUser
     * @return 用户信息
     */
    public AdminUser resetUser(AdminUser adminUser, String redisKey) {
        if (Objects.nonNull(adminUser)) {
            // if user exists, reset user information
            adminUser = AdminUser.dao.findFirst(Db.getSql("admin.user.queryUserByUserId"), adminUser.getUserId());
            List<AdminRole> roles = adminRoleService.getRoleByUserId(adminUser.getUserId().intValue());
            adminUser.setRoles(roles.stream().map(AdminRole::getRoleId).map(Long::intValue).collect(Collectors.toList()));
            adminUser.setRoleNames(roles.stream().map(AdminRole::getRoleName).collect(Collectors.toList()));
            CrmUser crmUser = new CrmUser(adminUser, roles);
            Redis.use().setex(redisKey, 360000, crmUser);
        }
        return adminUser;
    }

    public R queryUserList(BasePageRequest<AdminUser> request, String roleId, String searchKey) {
        List<Long> deptIdList = new ArrayList<>();
        if (request.getData().getDeptId() != null) {
            deptIdList.add(request.getData().getDeptId().longValue());
            deptIdList.addAll(queryChileDeptIds(request.getData().getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
        }
        String searchName;
        if (searchKey == null || searchKey.isEmpty()) {
            searchName = request.getData().getRealname();
        } else {
            searchName = searchKey;
        }
        if (request.getPageType() == 0) {
            List<Record> recordList = Db.find(Db.getSqlPara("admin.user.queryUserList", Kv.by("name", searchName).set("deptId", deptIdList).set("status", request.getData().getStatus()).set("roleId", roleId)));
            return R.ok().put("data", recordList);
        } else {
            Page<Record> paginate = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.user.queryUserList", Kv.by("name", searchName).set("deptId", deptIdList).set("status", request.getData().getStatus()).set("roleId", roleId)));
            return R.ok().put("data", paginate);
        }
    }

    /**
     * 根据场景编码查询负责人用户列表
     * @param keyword
     * @param sceneCode
     * @param deptId
     * @return
     */
    public List<Record> queryUserListBySceneCode(String keyword, String sceneCode, Long deptId) {
        CrmCustomerSceneEnum sceneEnum = CrmCustomerSceneEnum.findByCode(sceneCode);
        if (Objects.isNull(sceneEnum)) {
            throw new CrmException("未知场景编码");
        }
        AdminUser currentUser = BaseUtil.getUser();
        if (Objects.isNull(currentUser)) {
            throw new CrmException("用户尚未登陆");
        }
        List<Long> sonDeptIds = null;
        if (Objects.nonNull(deptId)) {
            //如果传了部门ID
            Record deptTree = adminDeptService.getFromBizDeptTree(deptId);
            sonDeptIds = adminDeptService.loopDeptTreeForDeptIdList(deptTree);
        }
        Kv params = Kv.create();
        switch (sceneEnum) {
            case MINE_CUSTOMER:
                return Collections.singletonList(new Record().set("userId", currentUser.getUserId()).set("realname", currentUser.getRealname()).set("username", currentUser.getUsername()));
            case MY_SUBORDINATE_CUSTOMER:
                List<Long> userIds;
                if (currentUser.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    //查询下属用户ID
                    userIds = queryUserByParentUser(currentUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                } else {
                    //根据数据权限查询
                    userIds = queryUserByAuth(currentUser.getUserId());
                }
                if (CollectionUtils.isEmpty(userIds)) {
                    return Collections.emptyList();
                }
                params.set("userIds", userIds);
                break;
            case CUSTOMER_ALL:
            case DISTRIBUTOR_RELATE_POOL:
            case MOBILE_SALE_CUSTOMER:
            case CUSTOMER_CREATED_BY_ME:
            case TAKE_PART_CUSTOMER:
                //用户名称为空直接返回
                if (StringUtils.isBlank(keyword)) {
                    return Collections.emptyList();
                }
                break;
            default:
                //其他场景返回空列表
                return Collections.emptyList();
        }
        if (StringUtils.isNotBlank(keyword)) {
            params.set("name", keyword);
        }
        if (CollectionUtils.isNotEmpty(sonDeptIds)) {
            params.set("deptIds", sonDeptIds);
        }
        List<AdminUser> userList = AdminUser.dao.find(Db.getSqlPara("admin.user.filterUserList", params));
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }
        return userList.stream().map(adminUser -> new Record().set("userId", adminUser.getUserId()).set("realname", adminUser.getRealname()).set("username", adminUser.getUsername())).collect(Collectors.toList());
    }

    /**
     * 查询可设置为上级的user
     */
    public List<Record> queryTopUserList(Long userId) {
        List<Record> recordList = Db.find("select user_id,realname,parent_id from 72crm_admin_user");
        List<Long> subUserList = queryChileUserIds(userId, BaseConstant.AUTH_DATA_RECURSION_NUM);
        recordList.removeIf(record -> subUserList.contains(record.getLong("user_id")));
        recordList.removeIf(record -> record.getLong("user_id").equals(userId));
        return recordList;
    }

    /**
     * 查询本部门下的所有部门id
     *
     * @param deptId 当前部门id
     */
    public List<Long> queryChileDeptIds(Integer deptId, Integer deepness) {
        List<Long> list = Db.query("select dept_id from 72crm_admin_dept where pid = ?", deptId);
        if (list.size() != 0 && deepness > 0) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                list.addAll(queryChileDeptIds(list.get(i).intValue(), deepness - 1));
            }
        }
        return list;
    }

    /**
     * 查询本用户下的所有下级id
     *
     * @param userId 当前用户id
     */
    public List<Long> queryChileUserIds(Long userId, Integer deepness) {
        List<Long> query = Db.query("select user_id from 72crm_admin_user where parent_id = ?", userId);
        if (deepness > 0) {
            for (int i = 0, size = query.size(); i < size; i++) {
                query.addAll(queryChileUserIds(query.get(i), deepness - 1));
            }
        }
        HashSet<Long> set = new HashSet<>(query);
        query.clear();
        query.addAll(set);
        return query;
    }


    public R resetPassword(String ids, String pwd) {
        for (String id : ids.split(",")) {
            AdminUser adminUser = AdminUser.dao.findById(id);
            String password = BaseUtil.sign(adminUser.getUsername() + pwd, adminUser.getSalt());
            Db.update("update 72crm_admin_user set password = ? where user_id = ?", password, id);
        }
        return R.ok();
    }

    public R querySuperior(String realName) {
        return R.ok().put("data", Db.find(Db.getSqlPara("admin.user.querySuperior", Kv.by("name", realName))));
    }

    public R queryListName(String name) {

        StringBuilder sql = new StringBuilder("select  au.realname,au.mobile,au.post as postName ,ad.name as deptName from 72crm_admin_user as au\n" + "LEFT JOIN 72crm_admin_dept as ad on au.dept_id = ad.dept_id");
        if (name != null && !"".equals(name)) {
            sql.append(" where au.realname like '%").append(name).append("%'");
        }
        List<Record> records = Db.find(sql.toString());
        Sort sort = new Sort();
        Map<String, List<Record>> map = sort.sort(records);
        return R.ok().put("data", map);
    }

    /**
     * @author zxy
     * 查询部门属用户列表
     */
    public R queryListNameByDept(String name) {
        List<Record> records = Db.find(Db.getSql("admin.dept.deptSql"));
        for (Record record : records) {
            List<Record> users = Db.find(Db.getSqlPara("admin.user.queryUserByRealName", Kv.by("deptId", record.getInt("dept_id")).set("name", name)));
            record.set("userList", users);
            record.set("userNumber", users.size());
        }
        return R.ok().put("data", records);
    }

    /**
     * @author whh
     * 根据部门查询用户id
     */
    public List<Long> queryUserIdsByDeptIdList(List<Long> deptIds) {
        if (CollectionUtils.isEmpty(deptIds)) {
            return Collections.emptyList();
        }
        String sql = "select user_id from 72crm_admin_user where dept_id in (" + deptIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        List<Record> records = Db.find(sql);
        return records.stream().map(item -> item.getLong("user_id")).collect(Collectors.toList());
    }

    public R queryAllUserList() {
        List<Record> recordList = Db.find(Db.getSqlPara("admin.user.queryUserList"));
        return R.ok().put("data", recordList);
    }

    public R setUserStatus(String ids, String status) {
        for (Integer id : TagUtil.toSet(ids)) {
            Db.update("update 72crm_admin_user set status = ? where user_id = ?", status, id);
        }
        return R.ok();
    }

    public boolean updateImg(String url, Long userId) {
        Record r = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), userId);
        AdminUser adminUser = new AdminUser()._setAttrs(r.getColumns());
        adminUser.setImg(url);
        return adminUser.update();
    }

    public boolean updateUser(AdminUser adminUser) {
        if (!BaseUtil.getUser().getUsername().equals(adminUser.getUsername())) {
            return false;
        }
        adminUser.setUserId(BaseUtil.getUserId());
        if (StrUtil.isNotEmpty(adminUser.getPassword())) {
            adminUser.setSalt(IdUtil.simpleUUID());
            adminUser.setPassword(BaseUtil.sign((adminUser.getUsername().trim() + adminUser.getPassword().trim()), adminUser.getSalt()));
        }
        return adminUser.update();
    }

    public List<Long> queryUserByAuth(Long userId) {
        List<Long> adminUsers = new ArrayList<>();
        //查询用户数据权限，从高到低排序
        List<Integer> list = Db.query(Db.getSql("admin.role.queryDataTypeByUserId"), userId);
        if (list.size() == 0) {
            //无权限查询自己的数据
            adminUsers.add(userId);
            return adminUsers;
        }
        //拥有最高数据权限
        if (list.contains(CrmDataAuthEnum.ALL_TYPE_KEY.getTypes())) {
            return Db.query(Db.getSql("admin.user.getAllUserIds"));
        } else {
            String loginUserId = Objects.isNull(BaseUtil.getUserId()) ? null : BaseUtil.getUserId().toString();

            String myDeptAndSubKey = MY_DEPT_AND_SUB + loginUserId;
            String myselfAndSubKey = MY_SELF_AND_SUB + loginUserId;
            String myDeptKey = MY_DEPT + loginUserId;

            AdminUser adminUser =AdminUser.dao.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), userId);
            //本部门及下属部门
            if (list.contains(CrmDataAuthEnum.DEPARTMENT_BRANCH_TYPE_KEY.getTypes())) {
                List<Long> myDeptAndSubKeyList = Redis.use().get(myDeptAndSubKey);
                if (myDeptAndSubKeyList == null || myDeptAndSubKeyList.size() == 0) {
                    List<Record> records = adminDeptService.queryDeptByParentDept(adminUser.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                    List<Integer> deptIds = new ArrayList<>();
                    /*添加本部门ID*/
                    deptIds.add(adminUser.getDeptId());
                    records.forEach(record -> deptIds.add(record.getInt("id")));
                    List<Long> userIds = new ArrayList<>();
                    if (deptIds.size() > 0) {
                        SqlPara sqlPara = Db.getSqlPara("admin.user.queryUserIdByDeptId", Kv.by("deptIds", deptIds));
                        userIds = Db.query(sqlPara.getSql(), sqlPara.getPara());
                    }
                    adminUsers.addAll(userIds);
                    Redis.use().setex(myDeptAndSubKey, 3600, userIds);
                } else {
                    adminUsers.addAll(Redis.use().get(myDeptAndSubKey));
                }
            }

            //本人及下属
            if (list.contains(CrmDataAuthEnum.ONESELF_BRANCH_TYPE_KEY.getTypes())) {
                if (Redis.use().get(myselfAndSubKey) == null) {
                    List<Long> userIds = queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                    adminUsers.addAll(userIds);
                    Redis.use().setex(myselfAndSubKey, 3600, userIds);
                } else {
                    adminUsers.addAll(Redis.use().get(myselfAndSubKey));
                }
            }

            //本部门
            if (list.contains(CrmDataAuthEnum.DEPARTMENT_TYPE_KEY.getTypes())) {
                if (Redis.use().get(myDeptKey) == null) {
                    SqlPara sqlPara = Db.getSqlPara("admin.user.queryUserIdByDeptId", Kv.by("deptIds", adminUser.getDeptId()));
                    List<Long> userIds = Db.query(sqlPara.getSql(), sqlPara.getPara());
                    adminUsers.addAll(userIds);
                    Redis.use().setex(myDeptKey, 3600, userIds);
                } else {
                    adminUsers.addAll(Redis.use().get(myDeptKey));
                }
            }

            //本人
            adminUsers.add(adminUser.getUserId());
        }
        adminUsers.add(userId);
        return adminUsers.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 获取数据权限对象
     * @param userId
     * @return
     */
    public DataPermissionVO queryDataPermission(Long userId) {
        DataPermissionVO dataPermission = new DataPermissionVO();
        CrmDataAuthEnum crmDataAuthEnum = queryDataPermissionLevel(userId);
        if (crmDataAuthEnum != null) {
            dataPermission.setLevel(crmDataAuthEnum.getTypes());
        }
        dataPermission.setUserIdList(queryUserByAuth(userId));
        return dataPermission;
    }

    /**
     * 获取用户数据权限最高等级
     * @param userId
     * @return
     */
    public CrmDataAuthEnum queryDataPermissionLevel(Long userId) {
        List<Integer> list = Db.query(Db.getSql("admin.role.queryDataTypeByUserId"), userId);
        return CrmDataAuthEnum.findByType(list.stream().max(Integer::compareTo).orElse(null));
    }

    public List<Long> queryUserByParentUser(Long userId, Integer deepness) {
        List<Long> recordList = new ArrayList<>();
        if (deepness > 0) {
            List<Long> records = Db.query("SELECT b.user_id FROM 72crm_admin_user AS b WHERE b.parent_id = ?", userId);
            recordList.addAll(records);
            int size = recordList.size();
            for (int i = 0; i < size; i++) {
                recordList.addAll(queryUserByParentUser(recordList.get(i), deepness - 1));
            }
        }
        return recordList;
    }

    public List<Record> queryUserByDeptId(Integer deptId) {
        return Db.find(Db.getSql("admin.user.queryUserByDeptId"), deptId);
    }

    public List<Record> queryUserByDeptIdEliminateDisabledUser(Integer deptId) {
        return Db.find(Db.getSql("admin.user.queryUserByDeptIdEliminateDisabledUser"), deptId);
    }

    /**
     * 修改用户账号功能
     *
     * @param id       用户ID
     * @param username 新的用户名
     * @param password 新的密码
     * @return 操作状态
     */
    @Before(Tx.class)
    public R usernameEdit(Integer id, String username, String password) {
        Record r = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), id);
        AdminUser adminUser = new AdminUser()._setAttrs(r.getColumns());
        if (adminUser == null) {
            return R.error("用户不存在！");
        }
        if (adminUser.getUsername().equals(username)) {
            return R.error("账号不能和原账号相同");
        }
        Integer count = Db.queryInt("select count(*) from 72crm_admin_user where username = ?", username);
        if (count > 0) {
            return R.error("手机号重复！");
        }
        adminUser.setUsername(username);
        adminUser.setPassword(BaseUtil.sign(username + password, adminUser.getSalt()));
        return R.isSuccess(adminUser.update());
    }

    private String getUserIds(String deptIds, String userIds) {
        List<Record> allUsers = Db.find("select * from 72crm_admin_user where dept_id   NOT in ( ? ) and user_id in (?)", deptIds, userIds);
        userIds = null;
        for (Record user : allUsers) {
            if (userIds == null) {
                userIds = user.getStr("user_id");
            } else {
                userIds = deptIds + "," + user.getStr("user_id");
            }
        }
        return userIds;
    }

    /**
     * batch update users.
     *
     * @param users
     * @return
     */
    public boolean batchUpdateStaff(List<AdminUser> users) {
        if (users == null || users.size() < 1) {
            return false;
        }
        List<Record> records = convertUserModelToRecord(users);
        return Db.batchUpdate("72crm_admin_user", "user_id", records, users.size()).length > 0;
    }

    /**
     * batch update status for the specified users.
     *
     * @param userIds
     * @param value
     * @return
     */
    public boolean batchUpdateUserStatus(List<Long> userIds, int value) {
        if (userIds == null || userIds.size() < 1) {
            return false;
        }
        return Db.update(Db.getSqlPara("admin.user.updateUserStatus", Kv.by("value", value).set("userIds", userIds))) > 0;
    }

    /**
     * batch insert new users.
     *
     * @param users
     * @return
     */
    public boolean batchInsertNewUser(List<AdminUser> users) {
        if (users == null || users.size() < 1) {
            return false;
        }
        List<Record> records = convertUserModelToRecord(users);
        return Db.batchSave("72crm_admin_user", records, users.size()).length > 0;
    }

    /**
     * Convert AdminUser list to Record list.
     *
     * @param users
     * @return
     */
    private List<Record> convertUserModelToRecord(List<AdminUser> users) {
        return users.stream().map(Model::toRecord).collect(Collectors.toList());
    }

    /**
     * Delete user by user_id
     *
     * @return
     */
    public void deleteUserByUserId(Long userId) {
        Db.delete(Db.getSql("admin.user.deleteUserByUserId"), userId);
    }

    public Record getDeptInfoByUserName(String userName) {
        return Db.findFirst(Db.getSql("admin.user.getDeptInfoByUserName"), userName);
    }

    /**
     * 根据部门id查询本部门及下属部门的用户
     *
     * @param deptId 部门ID
     */
    public List<Long> queryMyDeptAndSubUserByDeptId(Integer deptId) {
        //查询本部门下级所有部门
        List<Record> records = adminDeptService.queryDeptByParentDept(deptId, BaseConstant.AUTH_DATA_RECURSION_NUM);
        List<Integer> deptIds = records.stream().map(record -> record.getInt("id")).collect(Collectors.toList());
        /*添加本部门ID*/
        deptIds.add(deptId);
        return getUserIdsByDeptIds(deptIds);
    }

    /***
     * 根据部门集合获取人员集合
     * @author yue.li
     * @param deptIds 部门ids
     * @return
     */
    public List<Long> getUserIdsByDeptIds(List<Integer> deptIds) {
        SqlPara sqlPara = Db.getSqlPara("admin.user.queryUserIdByDeptId", Kv.by("deptIds", deptIds));
        return Db.query(sqlPara.getSql(), sqlPara.getPara());
    }

    /**
     * 查询部门及下属部门
     *
     * @param deptId
     * @return
     */
    public List<Integer> queryMyDeptAndSubDeptId(Integer deptId) {

        List<Integer> deptIds = new ArrayList<>();
        //查询本部门下级所有部门
        List<Record> records = adminDeptService.queryDeptByParentDept(deptId, BaseConstant.AUTH_DATA_RECURSION_NUM);

        /*添加本部门ID*/
        deptIds.add(deptId);
        records.forEach(record -> deptIds.add(record.getInt("id")));

        return deptIds;
    }

    /**
     * 查询所有人员信息
     *
     * @return
     * @author yue.li
     */
    public List<Record> getAllUsers() {
        return Db.find(Db.getSql("admin.user.getAllUsers"));
    }

    /**
     * Return AdminUser by staff number.
     *
     * @param staffNo staff number
     * @return {@code AdminUser}
     */
    public AdminUser getUserByStaffNo(String staffNo) {
        if (StringUtils.isEmpty(staffNo)) {
            return null;
        }

        Record record = Db.findFirst(Db.getSql("admin.user.getUserByStaffNo"), staffNo);
        if (Objects.nonNull(record)) {
            return new AdminUser()._setAttrs(record.getColumns());
        } else {
            return null;
        }
    }

    /**
     * 根据域账号获取用户信息
     *
     * @param userName 用户域账号
     * @return
     * @author yue.li
     */
    public AdminUser getUserByUserName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return null;
        }

        Record record = Db.findFirst(Db.getSql("admin.user.getUserByUserName"), userName);
        if (Objects.nonNull(record)) {
            return new AdminUser()._setAttrs(record.getColumns());
        } else {
            return null;
        }
    }

    /**
     * 获取所有人员信息(id和用户域账号)
     *
     * @author yue.li
     */
    public List<AdminUser> queryUserInfos() {
        List<Record> recordList = Db.find(Db.getSql("admin.user.queryUserInfos"));
        if (CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyList();
        }
        return recordList.stream().map(item -> new AdminUser()._setAttrs(item.getColumns())).collect(Collectors.toList());
    }

    /**
     * 根据人员域账号集合获取人员ids
     *
     * @param userNames 人员域账号集合
     * @author yue.li
     */
    public List<Long> getUserIdsByUserNames(List<String> userNames) {
        List<Long> userIds = new ArrayList<>();
        List<AdminUser> adminUserList = queryUserInfos();
        if (CollectionUtils.isEmpty(adminUserList) || CollectionUtils.isEmpty(userNames)) {
            return Collections.emptyList();
        }
        userNames.forEach(userName -> adminUserList.forEach(adminUser -> {
            if (adminUser.getUsername().equals(userName)) {
                userIds.add(adminUser.getUserId());
            }
        }));
        return userIds;
    }

    /**
     * 根据用户id查询事业部对应的部门id
     *
     * @param userId
     * @return
     */
    public Long getBusinessDepartmentOfUserById(Long userId) {
        if (Objects.nonNull(userId)) {
            /*查找人所对应事业部*/
            Record ownUserRecord = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), userId);
            String businessDeptId = adminDeptService.getBusinessDepartmentByDeptId(ownUserRecord.getStr("dept_id"));
            if (businessDeptId != null) {
                return Long.valueOf(businessDeptId);
            }
        }
        return null;
    }

    /**
     * 通过用户id集合查询用户信息
     *
     * @param userIds
     * @return
     */
    public List<AdminUser> getUserListByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        List<AdminUser> adminUsers = AdminUser.dao.find(Db.getSqlPara("admin.user.getUserListByUserIds", Kv.by("userIds", userIds)));
        if (CollectionUtils.isEmpty(adminUsers)) {
            return Collections.emptyList();
        }
        return adminUsers;
    }

    /**
     * 通过部门集合查询用户信息集合
     *
     * @param deptIds
     * @return
     */
    public List<AdminUser> getUserListByDeptIds(List<Long> deptIds) {
        if (CollectionUtils.isEmpty(deptIds)) {
            return Collections.emptyList();
        }
        List<AdminUser> adminUsers = AdminUser.dao.find(Db.getSqlPara("admin.user.getUserListByDeptIds", Kv.by("deptIds", deptIds)));
        if (CollectionUtils.isEmpty(adminUsers)) {
            return Collections.emptyList();
        }
        return adminUsers;
    }

    /**
     * 通过用户ID查询用户信息
     *
     * @param userId
     * @return
     */
    public AdminUser getAdminUserByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        return AdminUser.dao.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), userId);
    }

    /**
     * 查询用户的部门ID
     * @param ownerUserId
     * @return
     */
    public Long queryDeptIdOfUser(Long ownerUserId) {
        return Db.queryLong(Db.getSql("admin.user.queryDeptIdOfUser"), ownerUserId);
    }
}
