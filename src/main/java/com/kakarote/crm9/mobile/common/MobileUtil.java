package com.kakarote.crm9.mobile.common;

import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.BaseUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Mobile Util
 *
 * @author hao.fu
 * @since 2019/12/25 10:16
 */
public class MobileUtil {

    /**
     * Get CRM token from mobile client.
     *
     * @param request request
     * @return crm token
     */
    public static String getCrmToken(HttpServletRequest request) {
        return request.getHeader(CrmConstant.CRM_TOKEN_FOR_MOBILE);
    }

    /**
     * Get CRM user from mobile request.
     *
     * @param request request
     * @return crm user
     */
    public static CrmUser getCrmUser(HttpServletRequest request) {
        String token = BaseUtil.LOGIN_CACHE + getCrmToken(request);
        if (StringUtils.isNotEmpty(token)) {
            return Redis.use().get(token);
        }
        return null;
    }

    public static boolean isEmptyUser(HttpServletRequest request) {
        CrmUser crmUser = MobileUtil.getCrmUser(request);
        return Objects.isNull(crmUser) || Objects.isNull(crmUser.getCrmAdminUser());
    }

}
