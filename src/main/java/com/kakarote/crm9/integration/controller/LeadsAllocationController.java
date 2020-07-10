package com.kakarote.crm9.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.integration.service.LeadsAllocationService;
import com.kakarote.crm9.utils.R;

/**
 * 资源分发引擎入口
 * @author xiaowen.wu
 *
 */
@Before(IocInterceptor.class)
public class LeadsAllocationController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Autowired
    private NotifyService notifyService;

    @Inject
    private AdminSendEmailService adminSendEmailService;
    
    @Inject
    private LeadsAllocationService leadsAllocationservice;
    
	/**
     * 资源分发
     */
    public void leadsAllocation() {
        try {
        	// 资源分发
			renderJson(leadsAllocationservice.leadsAllocation());
		} catch (Exception e) {
			logger.error("LeadsAllocationController -> leadsAllocation -> 处理异常",e);
            adminSendEmailService.sendErrorMessage(e, notifyService);
			renderJson(R.error("LeadsAllocationController -> leadsAllocation -> 处理异常"));
		}
    }
}
