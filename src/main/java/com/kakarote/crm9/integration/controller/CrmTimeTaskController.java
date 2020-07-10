package com.kakarote.crm9.integration.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.integration.service.CrmTimeTaskService;

/**
 * @Author: honglei.wan
 * @Description:crm定时任务控制类
 * @Date: Create in 2020/6/16 2:20 下午
 */
@Before(IocInterceptor.class)
public class CrmTimeTaskController extends Controller {

	@Inject
	private CrmTimeTaskService crmTimeTaskService;

	/**
	 * 合同付款条款数据传送给履约系统
	 */
	public void transferContractPaymentToAgreement(){
		renderJson(crmTimeTaskService.transferContractPaymentToAgreement());
	}

}
