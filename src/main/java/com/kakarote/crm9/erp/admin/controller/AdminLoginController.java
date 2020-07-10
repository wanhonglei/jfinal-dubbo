package com.kakarote.crm9.erp.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.UserExpandInfo;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.qxwz.buc.sso.client.web.BucWebUser;
import com.qxwz.buc.sso.client.web.BucWebUserUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户登录
 *
 * @author z
 */
public class AdminLoginController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminRoleService adminRoleService;
    @Inject
    private AdminDeptService adminDeptService;

    public void index() {
        redirect("/index.html");
    }

    /**
     * userInfo
     */
    public void userInfo() {
        BucWebUser bucUser = BucWebUserUtil.retrieveWebUser(getRequest());
        // buc user is null, sso login failed, return 302
        if(bucUser == null || bucUser.getEmail().isEmpty()) {
            renderJson(R.error(HttpStatus.SC_MOVED_TEMPORARILY, "Please login first!"));
            return;
        }

        String username = bucUser.getEmail().substring(0, bucUser.getEmail().indexOf('@'));
        AdminUser user = AdminUser.dao.findFirst(Db.getSql("admin.user.queryByUserName"), username);

        // user does not exist in crm db, maybe a new staff, data is not synced in time
        if (user == null) {
            renderJson(R.error(HttpStatus.SC_UNAUTHORIZED, String.format("This user does not exist in CRM system: %s, please contact administrator.", username)));
            return;
        }

        user.setLastLoginIp(BaseUtil.getLoginAddress(getRequest()));
        user.setLastLoginTime(new Date());
        user.update();

        List<AdminRole> roles = adminRoleService.getRoleByUserId(user.getUserId().intValue());
        if (roles == null || roles.size() < 1) {
            renderJson(R.error(HttpStatus.SC_UNAUTHORIZED, String.format("This user: %s has no role in CRM system, please contact administrator.", username)));
            return;
        }
        user.setRoles(roles.stream().map(AdminRole::getRoleId).map(Long::intValue).collect(Collectors.toList()));
        user.setRoleNames(roles.stream().map(AdminRole::getRoleName).collect(Collectors.toList()));
        /**存储一级事业部ID和名称*/
        String businessDepartmentId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(user.getDeptId()));
        String businessDepartmentName;
        Record record = null;
        UserExpandInfo userExpandInfo = new UserExpandInfo();
        if(StringUtils.isNotEmpty(businessDepartmentId)){
            record = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), businessDepartmentId);
        }
        if(record != null){
            businessDepartmentName = record.getStr("name");
            userExpandInfo.setBusinessDepartmentId(Integer.parseInt(businessDepartmentId));
            userExpandInfo.setBusinessDepartmentName(businessDepartmentName);
            userExpandInfo.setRoleNames(user.getRoleNames());
        }
        /**存储一级事业部ID和名称、是否网站用户*/

        logger.info("login user info: " + user.toJson());

        String token = BaseUtil.getToken(getRequest());
        CrmUser crmUser = new CrmUser(user, roles);
        crmUser.setUserExpandInfo(userExpandInfo);
        Redis.use().setex(token, 360000, crmUser);

        renderJson(R.ok().put(CrmConstant.BUC_AUTH_TOKEN_KEY, token).put("user", user).put("auth", adminRoleService.auth(user.getUserId())).put("userExpandInfo",userExpandInfo));
    }

    /**
     * @author zhangzhiwei
     * 退出登录
     */
    public void logout() {
        String token = BaseUtil.getToken(getRequest());
        if (!StrUtil.isEmpty(token)) {
            Redis.use().del(token);
            removeCookie(CrmConstant.BUC_AUTH_TOKEN_KEY);
        }
        renderJson(R.ok());
    }

    public void version() {
        renderJson(R.ok().put("name", BaseConstant.NAME).put("version", BaseConstant.VERSION));
    }

    public void ping() {
        List<String> arrays = new ArrayList<>();
        Connection connection = null;
        try {
            connection = Db.use().getConfig().getConnection();
            if (connection != null) {
                arrays.add("数据库连接成功");
            }
        } catch (Exception e) {
            arrays.add("数据库连接异常");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        try {
            String ping = Redis.use().ping();
            if ("PONG".equals(ping)) {
                arrays.add("Redis配置成功");
            } else {
                arrays.add("Redis配置失败");
            }
        } catch (Exception e) {
            arrays.add("Redis配置失败");
        }
        renderJson(R.ok().put("data", arrays));
    }
}
