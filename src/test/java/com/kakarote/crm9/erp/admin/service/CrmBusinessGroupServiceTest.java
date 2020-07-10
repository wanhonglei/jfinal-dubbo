package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessGroupDetailDto;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/9 9:35 上午
 */
public class CrmBusinessGroupServiceTest extends BaseTest {

    private CrmBusinessGroupService crmBusinessGroupService = Aop.get(CrmBusinessGroupService.class);

    @Test
    public void addBusinessGroup() {
        CrmBusinessGroupDetailDto crmBusinessGroupDetailDto = CrmBusinessGroupDetailDto.builder()
                .groupName("测试事业部")
                .groupEmail("5555@qq.com")
                .deptId(175L)
                .build();
        Assert.assertNotNull(crmBusinessGroupService.addBusinessGroup(crmBusinessGroupDetailDto));
    }

    @Test
    public void queryBusinessGroupList() {
        Assert.assertNotNull(crmBusinessGroupService.queryBusinessGroupList());
    }

    @Test
    public void queryBUsinessGroupDetail() {
        Assert.assertNotNull(crmBusinessGroupService.queryBUsinessGroupDetail(3L));
    }

    @Test
    public void checkDept(){
        Assert.assertNotNull(crmBusinessGroupService.checkDept(193L));
    }


    @Test
    public void updateBusinessGroup() {
        CrmBusinessGroupDetailDto crmBusinessGroupDetailDto = CrmBusinessGroupDetailDto.builder()
                .groupId(13L)
                .deptId(175L)
                .groupEmail("666@qq.com")
                .groupName("测试网站事业部").build();
        Assert.assertNotNull(crmBusinessGroupService.updateBusinessGroup(crmBusinessGroupDetailDto));
    }

    @Test
    public void checkEnableDelete(){
        Assert.assertNotNull(crmBusinessGroupService.checkEnableDelete(3L));
    }

    @Test
    public void deleteBusinessGroup() {
        Assert.assertNotNull(crmBusinessGroupService.checkEnableDelete(13L));
    }


    @Test
    public void getConfiguredDept() {
        System.out.println(crmBusinessGroupService.getConfiguredDept(186L));
    }
}