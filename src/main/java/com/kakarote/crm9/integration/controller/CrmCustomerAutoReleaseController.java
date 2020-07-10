package com.kakarote.crm9.integration.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.integration.service.CrmCustomerAutoReleaseService;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;

@Before(IocInterceptor.class)
@Slf4j
public class CrmCustomerAutoReleaseController extends Controller {

    @Inject
    private CrmCustomerAutoReleaseService crmCustomerAutoReleaseService;

    /**
     * 客户自动释放
     */
    public void customerAutoRelease() {
        R result = crmCustomerAutoReleaseService.customerAutoRelease();
        renderJson(result);
    }

}
