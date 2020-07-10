package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:yue.li Created by yue.li on 2020/03/10.
 */
public class AdminConfigServiceTest extends BaseTest {

    AdminConfigService adminConfigService = Aop.get(AdminConfigService.class);

    @Test
    public void addAdminConfig() {
        AdminConfig adminConfig = new  AdminConfig();
        adminConfig.setStatus(1);
        adminConfig.setName("测试" + UUID.randomUUID());
        adminConfig.setValue("2");
        adminConfig.setDescription("测试描述");
        adminConfigService.addAdminConfig(adminConfig);
        Assert.assertTrue(true);
    }

    @Test
    public void addBatchAdminConfig() {
        List<AdminConfig> adminConfigList = new ArrayList<>();
        AdminConfig adminConfig = new  AdminConfig();
        adminConfig.setStatus(1);
        adminConfig.setName("测试" + UUID.randomUUID());
        adminConfig.setValue("2");
        adminConfig.setDescription("测试描述");
        adminConfigList.add(adminConfig);
        adminConfigService.addBatchAdminConfig(adminConfigList);
        Assert.assertTrue(true);
    }

    @Test
    public void queryAdminConfig() {
        AdminConfig adminConfig = adminConfigService.queryAdminConfig("websiteCustomerPoolSetting");
        Assert.assertTrue(Objects.nonNull(adminConfig));
    }

    @Test
    public void queryCustomerReceivingSetting() {
        List<AdminConfig> adminConfigList = adminConfigService.queryCustomerReceivingSetting();
        Assert.assertTrue(adminConfigList.size() >0);
    }

    @Test
    public void updateConfigByName() {
        R r = adminConfigService.updateConfigByName("websiteCustomerPoolSetting","1");
        Assert.assertTrue( r.isSuccess());
    }

    @Test
    public void updateBatchConfigByName() {
        List<AdminConfig> adminConfigList = new ArrayList<>();
        AdminConfig adminConfig = new AdminConfig();
        adminConfig.setName("websiteCustomerPoolSetting");
        adminConfig.setValue("1");
        adminConfigList.add(adminConfig);
        R r = adminConfigService.updateBatchConfigByName(adminConfigList);
        Assert.assertTrue( r.isSuccess());
    }

    @Test
    public void getConfig() {
        Assert.assertNotNull(adminConfigService.getConfig(CrmConstant.TARGET_DEPT_PERFORMANCE_INCLUDED, "ss"));
    }
}
