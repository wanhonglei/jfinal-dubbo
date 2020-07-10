package com.kakarote.crm9.integration.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.rpc.annotation.RPCInject;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmContractPayment;
import com.kakarote.crm9.erp.crm.service.CrmContractPaymentService;
import com.kakarote.crm9.utils.R;
import com.qxwz.galaxy.api.request.BaseRequest;
import com.qxwz.galaxy.api.response.BaseResponse;
import com.qxwz.merak.billing.installment.api.CreateInstallmentBillBatchApi;
import com.qxwz.merak.billing.installment.model.request.CreateInstallmentBillBatchParam;
import com.qxwz.merak.billing.installment.model.request.bo.CreateInstallmentBillItemModel;
import com.qxwz.merak.billing.installment.model.request.bo.CreateInstallmentBillParentModel;
import com.qxwz.merak.billing.installment.model.response.CreateInstallmentBillBatchModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/16 2:36 下午
 */
@Slf4j
public class CrmTimeTaskService {

	@Inject
	private CrmContractPaymentService crmContractPaymentService;

	@RPCInject
	private CreateInstallmentBillBatchApi createInstallmentBillBatchApi;

	/**
	 * 合同付款条款数据传送给履约系统
	 */
	public R transferContractPaymentToAgreement(){
		log.info("do transferContractPaymentToAgreement begin ...");
		List<Record> contractList = CrmContract.dao.queryContractListWithNoSync();

		if (CollectionUtils.isEmpty(contractList)){
			log.info("可同步数量为0");
			return R.okWithData("可同步数量为0");
		}

		List<CreateInstallmentBillParentModel> parentModels = contractList.stream().map(o -> {
			CreateInstallmentBillParentModel parentModel = new CreateInstallmentBillParentModel();
			parentModel.setParentBizNo(o.getStr("parentBizNo"));
			parentModel.setUserId(o.getLong("userId"));
			parentModel.setBizType("contract_fulfill");
			parentModel.setTotalAmount(o.getBigDecimal("contractMoney"));

			Map<String, Object> map = new HashMap<>(1);
			map.put("oa", o.getStr("oaNum"));

			//查询合同下的条款信息
			List<CreateInstallmentBillItemModel> itemModelList = CrmContractPayment.dao.findListByColumn("contract_id", o.getLong("id"))
					.stream().map(payment -> {
						CreateInstallmentBillItemModel itemModel = new CreateInstallmentBillItemModel();
						itemModel.setBizNo(payment.getPaymentCode());
						itemModel.setExpectPayTime(payment.getPaymentDate());
						itemModel.setBillAmount(payment.getPaymentMoney());

						itemModel.setBizSpecialInfo(map);

						return itemModel;
					}).collect(Collectors.toList());

			parentModel.setItemModels(itemModelList);

			return parentModel;
		}).collect(Collectors.toList());


		CreateInstallmentBillBatchParam param = new CreateInstallmentBillBatchParam();
		param.setItemModels(parentModels);
		BaseRequest<CreateInstallmentBillBatchParam> request = new BaseRequest<>();
		request.setRequestId(IdUtil.simpleUUID());
		request.setCaller(CrmConstant.CRM_SR_CODE);
		request.setParam(param);


		log.info("合同付款条款数据传送给履约系统 param:{}", JSON.toJSONString(request));
		BaseResponse<CreateInstallmentBillBatchModel> response = createInstallmentBillBatchApi.createInstallmentBillBatch(request);
		log.info("合同付款条款数据传送给履约系统 response:{}", JSON.toJSONString(response));

		if (!response.isSuccess()){
			log.error("合同付款条款数据传送给履约系统 接口失败，code:{}，message:{}",response.getCode(),response.getMessage());
			return R.error(String.format("合同付款条款数据传送给履约系统 接口失败，code:%s，message:%s",response.getCode(),response.getMessage()));
		}else{
			crmContractPaymentService.updatePaymentAfterAgreement(response.getData());
		}

		log.info("do transferContractPaymentToAgreement end ...");

		return R.ok();
	}
}
