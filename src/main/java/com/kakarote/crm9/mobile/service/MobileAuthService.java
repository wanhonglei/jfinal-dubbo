package com.kakarote.crm9.mobile.service;

import com.google.common.collect.Maps;
import com.jfinal.aop.Inject;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.qxwz.buc.sso.client.filter.helper.LogonUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mobile Auth Service
 *
 * @author hao.fu
 * @since 2019/12/24 9:48
 */
public class MobileAuthService {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminRoleService adminRoleService;

    public R getCrmToken(HttpServletRequest request) {
        LogonUser bucUser = (LogonUser)request.getAttribute(CrmConstant.BUC_USER_KEY);
        // buc user is null, sso login failed, return 302
        if(Objects.isNull(bucUser) || bucUser.getEmail().isEmpty()) {
            return R.error(HttpStatus.SC_MOVED_TEMPORARILY, CrmConstant.INVALID_LOGIN_USER);
        } else {
            String token = generateMobileUserToken(bucUser);

            String username = bucUser.getEmail().substring(0, bucUser.getEmail().indexOf('@'));
            AdminUser user = AdminUser.dao.findFirst(Db.getSql("admin.user.queryByUserName"), username);
            if(Objects.isNull(user)) {
                return R.error(HttpStatus.SC_UNAUTHORIZED, CrmConstant.NO_CRM_PERMISSION);
            }
            List<AdminRole> roles = adminRoleService.getRoleByUserId(user.getUserId().intValue());
            if (CollectionUtils.isEmpty(roles)) {
                return R.error(HttpStatus.SC_UNAUTHORIZED, CrmConstant.NO_CRM_PERMISSION);
            }

            user.setRoles(adminRoleService.queryRoleIdsByUserId(user.getUserId()));
            CrmUser crmUser = new CrmUser(user, roles);

            // crm token expired in 24 hours.
            Redis.use().setex(BaseUtil.LOGIN_CACHE + token, 3600 * 24, crmUser);
            logger.info(String.format("issue crm mobile token for: %s, %s", username, token));
            Map<String, String> map = Maps.newHashMap();
            map.put("token", token);
            return R.ok().put("data", map);
        }
    }

    private String generateMobileUserToken(LogonUser bucUser) {
        String stringToSign = bucUser.getEmail() + bucUser.getStaffNo() + System.currentTimeMillis();
        return DigestUtils.md5Hex(stringToSign.getBytes(StandardCharsets.UTF_8));
    }

}
