package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.dto.CrmPaymentDto;
import org.junit.Test;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/13 10:53 上午
 */
public class CrmPaymentServiceTest extends BaseTest {

	private CrmPaymentService crmPaymentService = Aop.get(CrmPaymentService.class);

	@Test
	public void bindPayment() {
		JSONObject request = new JSONObject();
		request.put("bopsPaymentId","58f05a62135a45b2a4d819e78d021343");
		request.put("crmCustomerId",800159);
		request.put("businessId",23);
		request.put("planId",1403);

		crmPaymentService.bindPayment(request);
	}

	@Test
	public void bindCustomerManually() {

		crmPaymentService.bindCustomerManually("800159","58f05a62135a45b2a4d819e78d021343");
	}

	@Test
	public void updatePaymentStatus() {
		crmPaymentService.updatePaymentStatus("58f05a62135a45b2a4d819e78d021343",2);
	}

	@Test
	public void queryPaymentPageList() {
		BasePageRequest<CrmPaymentDto> basePageRequest = new BasePageRequest<>(1,10,new CrmPaymentDto());
		Long userId = 2441L;

		crmPaymentService.queryPaymentPageList(basePageRequest,userId);
	}

	@Test
	public void updateReceivablePlan() {
		JSONObject request = new JSONObject();
		request.put("planId",1403);
		request.put("winRateId",800159);
		request.put("loseReasonId",23);
		request.put("remark","单测用例");


		crmPaymentService.updateReceivablePlan(request);
	}

	@Test
	public void relieve() {
		crmPaymentService.relieve("58f05a62135a45b2a4d819e78d021343","800159","1212");
	}

	@Test
	public void queryByBopsPaymentId() {
		crmPaymentService.queryByBopsPaymentId("58f05a62135a45b2a4d819e78d021343");
	}
}
