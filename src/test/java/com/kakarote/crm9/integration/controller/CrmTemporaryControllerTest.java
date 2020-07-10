//package com.kakarote.crm9.integration.controller;
//
//import com.jfinal.aop.Aop;
//import com.kakarote.crm9.BaseControllerTest;
//import com.kakarote.crm9.common.config.JfinalConfig;
//import io.undertow.servlet.spec.HttpServletRequestImpl;
//import org.junit.Test;
//
//import javax.servlet.http.HttpServletRequest;
//
///**
// * @Author: haihong.wu
// * @Date: 2020/6/5 9:35 上午
// */
//public class CrmTemporaryControllerTest extends BaseControllerTest<JfinalConfig> {
//
//    private CrmTemporaryController crmTemporaryController = Aop.get(CrmTemporaryController.class);
//
//    @Test
//    public void execute() throws InterruptedException {
//        HttpServletRequest request = mockRequestWithUser("1011");
//        String url = "/crm/integration/temporary/execute";
//        System.out.println(use(url).invoke((HttpServletRequestImpl) request));
//    }
//}