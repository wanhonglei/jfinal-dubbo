package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/13 4:33 下午
 */
public class AdminFileServiceTest extends BaseTest {

    private AdminFileService adminFileService = Aop.get(AdminFileService.class);

    @Test
    public void updateBatchIdById() {
        adminFileService.updateBatchIdById(2L, "testBatchId");
        Assert.assertTrue(true);
    }

    @Test
    public void queryIdsByBatchId() {
        System.out.println(adminFileService.queryIdsByBatchId("testBatchId"));
        Assert.assertTrue(true);
    }
}