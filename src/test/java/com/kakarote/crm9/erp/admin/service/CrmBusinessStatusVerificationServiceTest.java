package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/13 4:41 下午
 */
public class CrmBusinessStatusVerificationServiceTest extends BaseTest {

    private CrmBusinessStatusVerificationService crmBusinessStatusVerificationService = Aop.get(CrmBusinessStatusVerificationService.class);

    @Test
    public void countRecordByStatusIdOfBusiness() {
        System.out.println(crmBusinessStatusVerificationService.countRecordByStatusIdOfBusiness(185L, 118L));
        Assert.assertTrue(true);
    }

    @Test
    public void findRecordByBizIdAndVeriId() {
        System.out.println(crmBusinessStatusVerificationService.findRecordByBizIdAndVeriId(185L, 1L));
        Assert.assertTrue(true);
    }

    @Test
    public void listByStatusIdOfBusiness() {
        crmBusinessStatusVerificationService.listByStatusIdOfBusiness(1L,1L);
    }

    @Test
    public void selectAllByStatusId() {
        crmBusinessStatusVerificationService.selectAllByStatusId(1L);
    }

    @Test
    public void selectVerificationRecordsByVerificationIds() {
        crmBusinessStatusVerificationService.selectVerificationRecordsByVerificationIds(Collections.singletonList(1L));
    }
}