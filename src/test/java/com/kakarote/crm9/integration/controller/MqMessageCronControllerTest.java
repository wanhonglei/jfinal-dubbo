//package com.kakarote.crm9.integration.controller;
//
//import com.kakarote.crm9.BaseControllerTest;
//import com.kakarote.crm9.common.config.JfinalConfig;
//import io.undertow.servlet.spec.HttpServletRequestImpl;
//import org.junit.Test;
//
//import javax.servlet.http.HttpServletRequest;
//
///**
// * @Author: haihong.wu
// * @Date: 2020/5/12 8:22 下午
// */
//public class MqMessageCronControllerTest extends BaseControllerTest<JfinalConfig> {
//
//    @Test
//    public void handleMsg() {
//        HttpServletRequest request = mockRequestWithUser("1011");
//        String url = "/crm/integration/mq/handleMsg";
//        System.out.println(use(url).post("{}").invoke((HttpServletRequestImpl) request));
//    }
//}