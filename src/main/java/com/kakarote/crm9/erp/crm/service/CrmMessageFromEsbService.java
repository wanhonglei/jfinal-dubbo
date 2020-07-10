package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.crm.dto.BaseRequestForBops;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/4/9 10:21 上午
 */
@Slf4j
public class CrmMessageFromEsbService {

	/**
	 * 查询订单列表 - order
	 * @param baseRequestForBops
	 */
	public JSONObject queryOrderList(BaseRequestForBops baseRequestForBops) {
		String jsonString = JSON.toJSONString(baseRequestForBops, SerializerFeature.WriteMapNullValue);
		log.info("queryBopsOrderList request param: {}", jsonString);
		String responseString;
		try {
			responseString = HttpUtil.post(EsbConfig.OrderApiEnum.ORDER_QUERY.getUrl(), jsonString, EsbConfig.OrderApiEnum.ORDER_QUERY.buildEsbHeader());
			log.info("queryBopsOrderList responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}

	/**
	 * 查询服务实例列表 - order
	 * @param baseRequestForBops
	 */
	public JSONObject queryInstanceList(BaseRequestForBops baseRequestForBops) {
		String jsonString = JSON.toJSONString(baseRequestForBops, SerializerFeature.WriteMapNullValue);
		log.info("queryBopsInstanceList request param: {}", jsonString);
		String responseString;
		try {
			responseString = HttpUtil.post(EsbConfig.OrderApiEnum.INSTANCE_QUERY.getUrl(), jsonString, EsbConfig.OrderApiEnum.INSTANCE_QUERY.buildEsbHeader());
			log.info("queryBopsInstanceList responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}

	/**
	 * 订单列表总金额查询 - order
	 * @param baseRequestForBops
	 */
	public JSONObject queryOrderSummaryList(BaseRequestForBops baseRequestForBops) {
		String jsonString = JSON.toJSONString(baseRequestForBops, SerializerFeature.WriteMapNullValue);
		log.info("queryBopsOrderSummaryList request param: {}", jsonString);
		String responseString;
		try {
			responseString = HttpUtil.post(EsbConfig.OrderApiEnum.SUMMARY_QUERY.getUrl(), jsonString, EsbConfig.OrderApiEnum.SUMMARY_QUERY.buildEsbHeader());
			log.info("queryBopsOrderSummaryList responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}

	/**
	 * 查询发票列表信息 - bops
	 * @param baseRequestForBops
	 */
	public JSONObject queryBopsInvoiceList(BaseRequestForBops baseRequestForBops) {
		String jsonString = JSON.toJSONString(baseRequestForBops, SerializerFeature.WriteMapNullValue);
		log.info("queryBopsInvoiceList request param: {}", jsonString);
		String responseString;
		try {
			responseString = HttpUtil.post(EsbConfig.BopsApiEnum.QUERY_INVOICE_RECORD_PAGE.getUrl(), jsonString, EsbConfig.BopsApiEnum.QUERY_INVOICE_RECORD_PAGE.buildEsbHeader());
			log.info("queryBopsInvoiceList responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}

	/**
	 * 查询发票信息列表总金额 - bops
	 * @param baseRequestForBops
	 */
	public JSONObject queryBopsInvoiceTotalAmountList(BaseRequestForBops baseRequestForBops) {
		String jsonString = JSON.toJSONString(baseRequestForBops, SerializerFeature.WriteMapNullValue);
		log.info("queryBopsInvoiceTotalAmountList request param: {}", jsonString);
		String responseString;
		try {
			responseString = HttpUtil.post(EsbConfig.BopsApiEnum.QUERY_INVOICE_RECORD_TOTAL_AMOUNT.getUrl(), jsonString, EsbConfig.BopsApiEnum.QUERY_INVOICE_RECORD_TOTAL_AMOUNT.buildEsbHeader());
			log.info("queryBopsInvoiceTotalAmountList responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}

	/**
	 * 查询兑换码信息列表 - bops
	 * @param baseRequestForBops
	 */
	public JSONObject queryBopsRedeemCodeList(BaseRequestForBops baseRequestForBops) {
		Map<String, String> stringObjectMap = new HashMap<>();
		if(baseRequestForBops.getPaged().getPageNum() != null){
			stringObjectMap.put("pageNo",String.valueOf(baseRequestForBops.getPaged().getPageNum()));
		}
		if(baseRequestForBops.getPaged().getPageSize() != null){
			stringObjectMap.put("pageSize",String.valueOf(baseRequestForBops.getPaged().getPageSize()));
		}
		if(baseRequestForBops.getParam().getUserIdList() != null){
			stringObjectMap.put("userIdList", StringUtils.join(baseRequestForBops.getParam().getUserIdList(),','));
		}
		if(baseRequestForBops.getParam().getStatus()!= null){
			stringObjectMap.put("status", String.valueOf(baseRequestForBops.getParam().getStatus()));
		}

		log.info("queryBopsRedeemCodeList request param: {}", stringObjectMap);
		String responseString;
		try {
			responseString = HttpUtil.get(EsbConfig.BopsApiEnum.QUERY_REDEEM_CODE.getUrl(), stringObjectMap, EsbConfig.BopsApiEnum.QUERY_REDEEM_CODE.buildEsbHeader());
			log.info("queryBopsRedeemCodeList responseMsg: {}", responseString);
			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}

	/**
	 * 查询支付信息列表 - bops
	 * @param baseRequestForBops
	 */
	public JSONObject queryBopsPaymentList(BaseRequestForBops baseRequestForBops) {
		String jsonString = JSON.toJSONString(baseRequestForBops, SerializerFeature.WriteMapNullValue);
		log.info("queryBopsPaymentList request param: {}", jsonString);
		String responseString;
		try {
			responseString = HttpUtil.post(EsbConfig.BopsApiEnum.QUERY_PAYMENT_INFORMATION.getUrl(), jsonString, EsbConfig.BopsApiEnum.QUERY_PAYMENT_INFORMATION.buildEsbHeader());
			log.info("queryBopsPaymentList responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}


	/**
	 * 查询优惠券信息接口 - bops
	 * @param baseRequestForBops
	 */
	public JSONObject queryBopsCouponsList(BaseRequestForBops baseRequestForBops) {
		String jsonString = JSON.toJSONString(baseRequestForBops, SerializerFeature.WriteMapNullValue);
		log.info("queryBopsCouponsList request param: {}", jsonString);
		String responseString;
		try {
			responseString = HttpUtil.post(EsbConfig.BopsApiEnum.QUERY_COUPONS.getUrl(), jsonString, EsbConfig.BopsApiEnum.QUERY_COUPONS.buildEsbHeader());
			log.info("queryBopsCouponsList responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}

	/**
	 * 合同付款条款数据传送给履约系统
	 */
	public JSONObject transferContractPaymentToAgreement(JSONObject jsonObject) {
		log.info("transferContractPaymentToAgreement request param: {}", jsonObject);
		String responseString;
		try {
			responseString = HttpUtil.post(EsbConfig.FastRegisterEnum.CREATE_BATCH_INSTALLMENT_BILL.getUrl(), jsonObject.toJSONString(), EsbConfig.FastRegisterEnum.CREATE_BATCH_INSTALLMENT_BILL.buildEsbHeader());
			log.info("transferContractPaymentToAgreement responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}

	/**
	 * 根据订单号获取BOPS订单信息 - bops
	 * @param orderNo
	 */
	public JSONObject queryOrderDetail(String orderNo) {
		log.info("queryOrderDetail request orderNo: {}", orderNo);
		String responseString;
		try {
			Map<String, String> stringObjectMap = new HashMap<>(1);
			stringObjectMap.put("orderNo",orderNo);

			responseString = HttpUtil.get(EsbConfig.BopsApiEnum.ORDER_DETAIL_QUERY.getUrl() ,stringObjectMap, EsbConfig.BopsApiEnum.ORDER_DETAIL_QUERY.buildEsbHeader());
			log.info("queryBopsCouponsList responseMsg: {}", responseString);

			return JSON.parseObject(responseString);
		} catch (Exception e) {
			log.error("esb调用异常",e);
			throw new CrmException(e);
		}
	}
}
