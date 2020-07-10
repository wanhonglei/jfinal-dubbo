package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminBusinessStatusType;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.Test;


/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:yue.li Created by yue.li on 2019/8/6.
 */
public class AdminBusinessStatusTypeServiceTest extends BaseTest {

    AdminBusinessStatusTypeService adminBusinessTypeService = Aop.get(AdminBusinessStatusTypeService.class);

    @Test
    public void addOrUpdateBusinessStatusType() {
        AdminBusinessStatusType adminBusinessStatusType = new AdminBusinessStatusType();
        adminBusinessStatusType.setStatusTypeName("2");
        adminBusinessStatusType.setWinRateValue("2");
        adminBusinessTypeService.addOrUpdateBusinessStatusType(adminBusinessStatusType,Long.valueOf(3));
    }

    @Test
    public void sealById() {
        String ids = "11";
        R r = adminBusinessTypeService.sealById(ids);
        Assert.assertTrue( r.isSuccess());
    }

    @Test
    public void unSealById() {
        String ids = "11";
        R r = adminBusinessTypeService.unSealById(ids);
        Assert.assertTrue( r.isSuccess());
    }
}
