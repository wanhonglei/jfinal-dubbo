package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/13 4:39 下午
 */
public class CrmBusinessStatusSalesActivityServiceTest extends BaseTest {

    private CrmBusinessStatusSalesActivityService crmBusinessStatusSalesActivityService = Aop.get(CrmBusinessStatusSalesActivityService.class);

    @Test
    public void listByStatusIdOfBusiness() {
        System.out.println(crmBusinessStatusSalesActivityService.listByStatusIdOfBusiness(185L, 118L));
        Assert.assertTrue(true);
    }

    @Test
    public void listRecordByStatusIdOfBusiness() {
        System.out.println(crmBusinessStatusSalesActivityService.listRecordByStatusIdOfBusiness(185L, 118L));
        Assert.assertTrue(true);
    }

    @Test
    public void listByStatusId() {
        crmBusinessStatusSalesActivityService.listByStatusId(1L);
    }

    @Test
    public void selectAllByStatusId() {
        crmBusinessStatusSalesActivityService.selectAllByStatusId(1L);
    }

    @Test
    public void selectActivityRecordsByActivityIds() {
        crmBusinessStatusSalesActivityService.selectActivityRecordsByActivityIds(Collections.singletonList(1L));
    }

    @Test
    public void countRecordByStatusIdOfBusiness() {
        crmBusinessStatusSalesActivityService.countRecordByStatusIdOfBusiness(1L,1L);
    }
}