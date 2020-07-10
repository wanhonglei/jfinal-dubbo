package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.qxwz.merak.billing.installment.model.response.CreateInstallmentBillBatchModel;
import org.junit.Test;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/16 7:26 下午
 */
public class CrmContractPaymentServiceTest extends BaseTest {

	private CrmContractPaymentService crmContractPaymentService = Aop.get(CrmContractPaymentService.class);

	@Test
	public void updatePaymentAfterAgreement() {
		String jsonString = "{\"failedExecRstModels\":[],\"successExecRstModels\":[{\"billNo\":\"IB-051wiubfn277k\",\"bizNo\":\"p-1273538341559734272\",\"bizType\":\"contract_fulfill\",\"success\":false},{\"billNo\":\"IB-051wiubkpcsg0\",\"bizNo\":\"p-1273538341928833024\",\"bizType\":\"contract_fulfill\",\"success\":false}]}";
		CreateInstallmentBillBatchModel model = JSONObject.parseObject(jsonString,CreateInstallmentBillBatchModel.class);
		crmContractPaymentService.updatePaymentAfterAgreement(model);
	}
}