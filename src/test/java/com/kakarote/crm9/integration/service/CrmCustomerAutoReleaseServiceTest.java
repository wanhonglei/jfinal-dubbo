package com.kakarote.crm9.integration.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.Test;

public class CrmCustomerAutoReleaseServiceTest  extends BaseTest {

    CrmCustomerAutoReleaseService crmCustomerAutoReleaseService = Aop.get(CrmCustomerAutoReleaseService.class);

    @Test
    public void customerAutoRelease() {
        R result = crmCustomerAutoReleaseService.customerAutoRelease();
        Assert.assertTrue(result.isSuccess());
    }

}
