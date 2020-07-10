package com.kakarote.crm9.erp.admin.service;

import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessStatusDto;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessStatusSalesActivityDto;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessStatusVerificationDto;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * CrmBusinessStatusService 单测用例
 */
public class CrmBusinessStatusServiceTest extends BaseTest {

    private CrmBusinessStatusService crmBusinessStatusService = Aop.get(CrmBusinessStatusService.class);


    @Test
    public void queryBusinessStatusList() {
        Assert.assertNotNull(crmBusinessStatusService.queryBusinessStatusList(2L));
    }

    @Test
    public void selectAllByTypeId() {
        Assert.assertNotNull(crmBusinessStatusService.selectAllByTypeId(2L));
    }


    @Test
    public void addBusinessStatus() {
        Db.tx(()->{
            CrmBusinessStatusDto crmBusinessStatusDto = CrmBusinessStatusDto.builder()
                    .groupId(12L)
                    .statusName("计划2")
                    .rate(new BigDecimal(60))
                    .opened(1)
                    .build();
            List<CrmBusinessStatusSalesActivityDto> activityList = Lists.newArrayList();
            CrmBusinessStatusSalesActivityDto activityDto = CrmBusinessStatusSalesActivityDto.builder()
                    .activityName("活动1").build();
            activityList.add(activityDto);
            activityDto = CrmBusinessStatusSalesActivityDto.builder()
                    .activityName("活动2").build();
            activityList.add(activityDto);
            List<CrmBusinessStatusVerificationDto> verificationList = Lists.newArrayList();
            CrmBusinessStatusVerificationDto verificationDto = CrmBusinessStatusVerificationDto.builder()
                    .verificationName("结果1").build();
            verificationList.add(verificationDto);
            verificationDto = CrmBusinessStatusVerificationDto.builder()
                    .verificationName("结果2").build();
            verificationList.add(verificationDto);
            crmBusinessStatusDto.setActivityList(activityList);
            crmBusinessStatusDto.setVerificationList(verificationList);
            Assert.assertNotNull(crmBusinessStatusService.addBusinessStatus(crmBusinessStatusDto));
            return false;
        });
    }

    @Test
    public void queryBusinessStatusDetail() {
        Assert.assertNotNull(crmBusinessStatusService.queryBusinessStatusDetail(13L));
    }

    @Test
    public void updateBusinessStatusInfo() {
        CrmBusinessStatusDto crmBusinessStatusDto = CrmBusinessStatusDto.builder()
                .groupId(12L)
                .statusId(71L)
                .statusName("计划3")
                .rate(new BigDecimal(60))
                .opened(1)
                .build();
        List<CrmBusinessStatusSalesActivityDto> activityList = Lists.newArrayList();
        CrmBusinessStatusSalesActivityDto activityDto = CrmBusinessStatusSalesActivityDto.builder()
                .activityName("活动1").build();
        activityList.add(activityDto);
        activityDto = CrmBusinessStatusSalesActivityDto.builder()
                .activityName("活动2").build();
        activityList.add(activityDto);
        List<CrmBusinessStatusVerificationDto> verificationList = Lists.newArrayList();
        CrmBusinessStatusVerificationDto verificationDto = CrmBusinessStatusVerificationDto.builder()
                .verificationName("结果1").build();
        verificationList.add(verificationDto);
        verificationDto = CrmBusinessStatusVerificationDto.builder()
                .verificationName("结果2").build();
        verificationList.add(verificationDto);
        crmBusinessStatusDto.setActivityList(activityList);
        crmBusinessStatusDto.setVerificationList(verificationList);
        Assert.assertNotNull(crmBusinessStatusService.updateBusinessStatusInfo(crmBusinessStatusDto));
    }

    @Test
    public void checkEnableClose() {
        Assert.assertNotNull(crmBusinessStatusService.checkEnableClose(71L));
    }

    @Test
    public void closeBusinessStatus() {
        Assert.assertNotNull(crmBusinessStatusService.closeBusinessStatus(71L));
    }

    @Test
    public void openBusinessStatus() {
        Assert.assertNotNull(crmBusinessStatusService.openBusinessStatus(71L));
    }

    @Test
    public void deleteBusinessStatus() {
        Assert.assertNotNull(crmBusinessStatusService.deleteBusinessStatus(71L));
    }

    @Test
    public void selectByTypeIdAndOrderNum() {
        Assert.assertNotNull(crmBusinessStatusService.selectByTypeIdAndOrderNum(2, 1));
    }

    @Test
    public void getByStatusId() {
        crmBusinessStatusService.getByStatusId(57L);
    }

    @Test
    public void checkEnableDelete() {
        crmBusinessStatusService.checkEnableDelete(13L);
    }

    @Test
    public void getStatusNameById() {
        crmBusinessStatusService.getStatusNameById(13L);
    }
}
