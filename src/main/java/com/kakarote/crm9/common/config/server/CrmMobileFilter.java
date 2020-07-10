package com.kakarote.crm9.common.config.server;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.jfinal.log.Log;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.mobile.common.MobileUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.qxwz.buc.sso.client.filter.helper.LogonUser;
import com.qxwz.buc.sso.client.filter.helper.SSOFilterHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

/**
 * Crm Mobile Filter
 *
 * @author hao.fu
 * @since 2019/12/24 10:19
 */
public class CrmMobileFilter implements Filter {

    private Log logger = Log.getLog(getClass());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsps = (HttpServletResponse) response;
        String requestUrl = req.getRequestURI();

        logger.info("mobile request url: " + req.getMethod() + " " + requestUrl);
        logger.info("mobile request getQueryString: " + req.getQueryString());
        Map<String, String> params = getAllRequestParam(req);
        params.keySet().forEach(key -> logger.info("mobile request params -> " + key + ":" + params.get(key)));

        R result = R.error(HttpStatus.SC_MOVED_TEMPORARILY, "Please login first!");
        // crm-token does not exist in header means need do sso login
        String crmMobileToken = getCrmMobileTokenFromRequest(req);
        if (StringUtils.isEmpty(crmMobileToken) || Redis.use().get(crmMobileToken) == null) {
            LogonUser bucUser = SSOFilterHelper.retrieveBucWebUser(req);
            if (bucUser == null || Strings.isNullOrEmpty(bucUser.getStaffNo()) || Strings.isNullOrEmpty(bucUser.getEmail())) {
                logger.info("CrmMobileFilter doFilter. bucUser is null. reqUrl:" + requestUrl);
            } else {
                // 如果移动端非getMobileToken方法需校验
                if(StringUtils.isNotEmpty(requestUrl) && !requestUrl.startsWith(CrmConstant.GET_MOBILE_TOKEN) && Objects.isNull(MobileUtil.getCrmUser(req))){
                    logger.info("#############clear mobile bucToken for: " + crmMobileToken);
                    Redis.use().del(crmMobileToken);
                    result = R.error(HttpStatus.SC_MOVED_TEMPORARILY, "check crmUser is null,please login first!");
                }else{
                    // mobile auth request will be handled here
                    logger.info("CrmMobileFilter doFilter. reqUrl:" + requestUrl + ", bucUser:" + bucUser);
                    req.setAttribute(CrmConstant.BUC_USER_KEY, bucUser);
                    chain.doFilter(req, rsps);
                    return;
                }
            }
        } else {
            chain.doFilter(req, rsps);
            return;
        }

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(JSONObject.toJSONString(result));

    }

    /**
     * Get crm mobile token from http servlet request.
     *
     * @param request HttpServletRequest
     * @return token
     */
    private String getCrmMobileTokenFromRequest(HttpServletRequest request) {
        return request.getHeader(CrmConstant.CRM_TOKEN_FOR_MOBILE) != null ? BaseUtil.LOGIN_CACHE + request.getHeader(CrmConstant.CRM_TOKEN_FOR_MOBILE) : "";
    }

    /**
     * 获取客户端请求参数中所有的信息
     *
     * @param request
     * @return      
     */
    private Map<String, String> getAllRequestParam(final HttpServletRequest request) {
        Map<String, String> res = Maps.newHashMap();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                //如果字段的值为空，判断若值为空，则删除这个字段>
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }


}
