package com.kakarote.crm9.integration.service;

import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Bops Payment Service Test
 *
 * @author hao.fu
 * @since 2019/11/13 10:18
 */
public class BopsPaymentServiceTest extends BaseTest {

    BopsPaymentService bopsPaymentService = Aop.get(BopsPaymentService.class);

    @Test
    public void getPaymentsByPaymentNo() {
        List<String> paymentNos = Lists.newArrayList();
        paymentNos.add("W4200000382201909243199331256");
        paymentNos.add("W4200000382201909243199331255");

        bopsPaymentService.getPaymentsByPaymentNo(paymentNos);
    }

    @Test
    public void getNoOrderPayment() {
        List<Record> records = bopsPaymentService.getNoOrderPayment();
        records.forEach(item -> System.out.println(item.toJson()));
        Assert.assertTrue(records.size() > 0);
    }

    @Test
    public void getAllPaymentNos() {
        List<String> records = bopsPaymentService.getAllPaymentNos();
        records.forEach(item -> System.out.println(item));
        Assert.assertTrue(records.size() > 0);
    }

}
