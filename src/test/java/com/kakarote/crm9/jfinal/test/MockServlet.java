package com.kakarote.crm9.jfinal.test;

import javax.servlet.*;
import java.io.IOException;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/13 4:49 下午
 */
public class MockServlet implements Servlet {
    @Override
    public void init(ServletConfig config) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
