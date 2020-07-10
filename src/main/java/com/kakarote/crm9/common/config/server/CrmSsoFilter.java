package com.kakarote.crm9.common.config.server;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.log.Log;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.qxwz.buc.sso.client.filter.helper.LogonUser;
import com.qxwz.buc.sso.client.filter.helper.SSOFilterHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Crm SSOFilter
 *
 * @author hao.fu
 * @create 2019/7/10 15:53
 */
public class CrmSsoFilter implements Filter {

    private Log logger = Log.getLog(getClass());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsps = (HttpServletResponse) response;
        String requestUrl = req.getRequestURI();

        //TODO for backend integration testing, should remove CORS handling in prod env.
        //---------START------------
        String origin = req.getHeader("Origin");
        if (StringUtils.isEmpty(origin)) {
            origin = "*";
        }
        rsps.addHeader("Access-Control-Allow-Credentials", "true");
        rsps.addHeader("Access-Control-Allow-Origin", origin);
        rsps.addHeader("Access-Control-Allow-Headers", "x-fgw-request-appid,x-fgw-request-env,x-requested-with,content-type");
        rsps.addHeader("Access-Control-Allow-Methods", "GET,HEAD,PUT,POST,DELETE,PATCH,OPTIONS");
        //---------END------------

        if (RequestMethod.OPTIONS.toString().equals(req.getMethod())) {
            rsps.setStatus(HttpStatus.SC_NO_CONTENT);
            return;
        } else {
            logger.info("crm filter: " + req.getMethod());
        }

        R result = R.error(HttpStatus.SC_MOVED_TEMPORARILY, "Please login first!");

        if(BaseUtil.needCheckLoginStatus(req)){
            LogonUser bucUser = SSOFilterHelper.retrieveBucWebUser(req);
            if (bucUser == null || Strings.isNullOrEmpty(bucUser.getStaffNo()) || Strings.isNullOrEmpty(bucUser.getEmail())) {
                logger.info("CrmSsoFilter doFilter. bucUser is null. reqUrl:" + requestUrl);
            } else {
                logger.info("CrmSsoFilter doFilter. reqUrl:" + requestUrl + ", bucUser:" + bucUser);
                req.setAttribute(CrmConstant.BUC_USER_KEY, bucUser);
                chain.doFilter(req, rsps);
                return;
            }
        }else{
            chain.doFilter(req, rsps);
            return;
        }
        doResponse(result, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        new SSOFilterHelper(filterConfig);
    }

    @Override
    public void destroy() {

      // Do nothing
    }

    private void doResponse(R result, ServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(JSONObject.toJSONString(result));
    }

}