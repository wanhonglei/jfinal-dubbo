package com.kakarote.crm9.integration.client;

import com.jfinal.aop.Aop;
import com.jfinal.kit.JsonKit;
import com.kakarote.crm9.BaseTest;
import org.junit.Test;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/19 11:35 上午
 */
public class PerformanceClientTest extends BaseTest {

    private PerformanceClient client = Aop.get(PerformanceClient.class);

    @Test
    public void listInstallmentBill() {
        System.out.println(JsonKit.toJson(client.listInstallmentBill("SC20200618173521QIET")));
    }

    @Test
    public void listInstallmentVerification() {
        System.out.println(JsonKit.toJson(client.listInstallmentVerification("SC20200618173521QIET",null)));
    }
}