package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.BaseRequestForBops;
import com.kakarote.crm9.erp.crm.dto.IParamForBops;
import com.kakarote.crm9.erp.crm.service.CrmMessageFromEsbService;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.R;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: honglei.wan
 * @Description:从esb获取信息方法控制类
 * @Date: Create in 2020/4/8 4:35 下午
 */
@Before(IocInterceptor.class)
public class CrmMessageFromEsbController extends Controller {

	@Autowired
	private EsbConfig esbConfig;

	@Inject
	private CrmMessageFromEsbService crmMessageFromEsbService;

	/**
	 * 查询订单列表
	 * userIds	List<Long>	网站用户ID（最多100）	可以
	 * orderType	Integer	订单类型	可以
	 * status	Integer	订单状态	可以
	 * pageSize	Integer	每页大小	可以（默认为10）
	 * pageNum	Integer	当前页数（从1开始）	可以（默认：1）
	 */
	public void queryOrderList(BasePageRequest<IParamForBops> basePageRequest) {
		if (basePageRequest == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}

		BaseRequestForBops.PageParamForBops pageParamForBops = new BaseRequestForBops.PageParamForBops();
		pageParamForBops.setPageNum(basePageRequest.getPage());
		pageParamForBops.setPageSize(basePageRequest.getLimit());

		BaseRequestForBops baseRequestForBops = new BaseRequestForBops();
		baseRequestForBops.setCaller(CrmConstant.CRM_SR_CODE);
		baseRequestForBops.setRequestId(IdUtil.simpleUUID());
		baseRequestForBops.setPaged(pageParamForBops);
		baseRequestForBops.setParam(basePageRequest.getData());

		renderJson(crmMessageFromEsbService.queryOrderList(baseRequestForBops));
	}

	/**
	 * 查询服务实例列表
	 * userIds List<Long> 用户ID集合	是 -> 不能为空，胡啸反应数据库可能报错
	 * ratingType	Integer	计费类型	是
	 * pageNum	Integer	当前页数（从1开始）	是，默认1
	 * pageSize	Integer	每页大小	是，默认10
	 */
	public void queryInstanceList(BasePageRequest<IParamForBops> basePageRequest) {
		if (basePageRequest == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}

		IParamForBops param = basePageRequest.getData();
		if (param == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}
		if (param.getUserIds() == null || param.getUserIds().size() == 0) {
			renderJson(R.error("请求参数:userIds 不能为空"));
			return;
		}

		BaseRequestForBops.PageParamForBops pageParamForBops = new BaseRequestForBops.PageParamForBops();
		pageParamForBops.setPageNum(basePageRequest.getPage());
		pageParamForBops.setPageSize(basePageRequest.getLimit());

		BaseRequestForBops baseRequestForBops = new BaseRequestForBops();
		baseRequestForBops.setCaller(CrmConstant.CRM_SR_CODE);
		baseRequestForBops.setRequestId(IdUtil.simpleUUID());
		baseRequestForBops.setPaged(pageParamForBops);
		baseRequestForBops.setParam(basePageRequest.getData());

		renderJson(crmMessageFromEsbService.queryInstanceList(baseRequestForBops));
	}


	/**
	 * 订单列表总金额查询
	 * userIds List<Long> 用户ID集合	是
	 * orderIds	List<Long>	订单ID集合	是
	 * orderNos	List<String>	订单编号集合	是
	 * status	Integer	支付状态	是
	 * refundStatus	Integer	退款状态	是
	 * orderType	Integer	订单类型	是
	 */
	public void queryOrderSummaryList(BasePageRequest<IParamForBops> basePageRequest) {
		if (basePageRequest == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}

		IParamForBops param = basePageRequest.getData();
		if (param == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}
		if (param.getUserIds() == null || param.getUserIds().size() == 0) {
			renderJson(R.error("请求参数:userIds 不能为空"));
			return;
		}

		BaseRequestForBops.PageParamForBops pageParamForBops = new BaseRequestForBops.PageParamForBops();
		pageParamForBops.setPageNum(basePageRequest.getPage());
		pageParamForBops.setPageSize(basePageRequest.getLimit());

		BaseRequestForBops baseRequestForBops = new BaseRequestForBops();
		baseRequestForBops.setCaller(CrmConstant.CRM_SR_CODE);
		baseRequestForBops.setRequestId(IdUtil.simpleUUID());
		baseRequestForBops.setPaged(pageParamForBops);
		baseRequestForBops.setParam(param);

		renderJson(crmMessageFromEsbService.queryOrderSummaryList(baseRequestForBops));
	}

	/**
	 * 查询发票列表信息
	 * sorts	List<Sort>	排序方式	可以
	 * userIdList	List<Long>	用户ID列表（最多100个）	不可以
	 * invoiceRecordStatus String	发票申请状态	可以
	 * invoiceChannel String	开票渠道	可以
	 * creatStartTime Date	发票申请日期开始	可以
	 * creatEndTime Date 发票申请日期结束 可以
	 * pageSize	Integer	每页大小	可以（默认为10）
	 * pageNum	Integer	当前页数（从1开始）	可以（默认：1）
	 */
	public void queryBopsInvoiceList(BasePageRequest<IParamForBops> basePageRequest) {
		if (basePageRequest == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}

		IParamForBops param = basePageRequest.getData();
		if (param == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}
		if (param.getUserIdList() == null || param.getUserIdList().size() == 0) {
			renderJson(R.error("请求参数:userIdList 不能为空"));
			return;
		}

		BaseRequestForBops.PageParamForBops pageParamForBops = new BaseRequestForBops.PageParamForBops();
		pageParamForBops.setPageNum(basePageRequest.getPage());
		pageParamForBops.setPageSize(basePageRequest.getLimit());

		BaseRequestForBops baseRequestForBops = new BaseRequestForBops();
		baseRequestForBops.setCaller(CrmConstant.CRM_SR_CODE);
		baseRequestForBops.setRequestId(IdUtil.simpleUUID());
		baseRequestForBops.setPaged(pageParamForBops);
		baseRequestForBops.setParam(basePageRequest.getData());

		renderJson(crmMessageFromEsbService.queryBopsInvoiceList(baseRequestForBops));
	}

	/**
	 * 查询发票信息列表总金额
	 * sorts	List<Sort>	排序方式	可以
	 * userIdList	List<Long>	用户ID列表（最多100个）	可以
	 * invoiceRecordStatus String	发票申请状态	可以
	 * invoiceChannel String	开票渠道	可以
	 * creatStartTime Date	发票申请日期开始	可以
	 * creatEndTime Date	 发票申请日期结束 可以
	 */
	public void queryBopsInvoiceTotalAmountList(BasePageRequest<IParamForBops> basePageRequest) {
		if (basePageRequest == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}

		BaseRequestForBops.PageParamForBops pageParamForBops = new BaseRequestForBops.PageParamForBops();
		pageParamForBops.setPageNum(basePageRequest.getPage());
		pageParamForBops.setPageSize(basePageRequest.getLimit());

		BaseRequestForBops baseRequestForBops = new BaseRequestForBops();
		baseRequestForBops.setCaller(CrmConstant.CRM_SR_CODE);
		baseRequestForBops.setRequestId(IdUtil.simpleUUID());
		baseRequestForBops.setPaged(pageParamForBops);
		baseRequestForBops.setParam(basePageRequest.getData());

		renderJson(crmMessageFromEsbService.queryBopsInvoiceTotalAmountList(baseRequestForBops));
	}

	/**
	 * 查询兑换码信息
	 * userIdList	List<Long>	用户ID列表（最多100个）	不可以
	 * status Integer	兑换码状态	可以
	 * pageSize	Integer	每页大小	可以（默认为10）
	 * pageNum	Integer	当前页数（从1开始）	可以（默认：1）
	 */
	public void queryBopsRedeemCodeList(BasePageRequest<IParamForBops> basePageRequest) {
		if (basePageRequest == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}

		IParamForBops param = basePageRequest.getData();
		if (param == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}
		if (param.getUserIdList() == null || param.getUserIdList().size() == 0) {
			renderJson(R.error("请求参数:userIdList 不能为空"));
			return;
		}

		BaseRequestForBops.PageParamForBops pageParamForBops = new BaseRequestForBops.PageParamForBops();
		pageParamForBops.setPageNum(basePageRequest.getPage());
		pageParamForBops.setPageSize(basePageRequest.getLimit());

		BaseRequestForBops baseRequestForBops = new BaseRequestForBops();
		baseRequestForBops.setCaller(CrmConstant.CRM_SR_CODE);
		baseRequestForBops.setRequestId(IdUtil.simpleUUID());
		baseRequestForBops.setPaged(pageParamForBops);
		baseRequestForBops.setParam(param);

		renderJson(crmMessageFromEsbService.queryBopsRedeemCodeList(baseRequestForBops));
	}

	/**
	 * 查询支付信息
	 * userIdList	List<Long>	用户ID列表（最多100个）	可以
	 * paymentNoList List<String>	交易流水号  可以
	 * orderNoList  List<String>	 支付关联的订单号 可以
	 * tradeNoList List<String>	 交易流水号  可以
	 * sellerList List<String>	 收款账户 可以
	 * paymentNoBankList List<String> 交易关联的银行流水号 可以
	 * paymentNoWeixinList List<String>	 交易关联的微信流水号 可以
	 * paymentNoAlipayList List<String>	 交易关联的支付宝流水号 可以
	 */
	public void queryBopsPaymentList(BasePageRequest<IParamForBops> basePageRequest) {
		if (basePageRequest == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}

		BaseRequestForBops.PageParamForBops pageParamForBops = new BaseRequestForBops.PageParamForBops();
		pageParamForBops.setPageNum(basePageRequest.getPage());
		pageParamForBops.setPageSize(basePageRequest.getLimit());

		BaseRequestForBops baseRequestForBops = new BaseRequestForBops();
		baseRequestForBops.setCaller(CrmConstant.CRM_SR_CODE);
		baseRequestForBops.setRequestId(IdUtil.simpleUUID());
		baseRequestForBops.setPaged(pageParamForBops);
		baseRequestForBops.setParam(basePageRequest.getData());

		renderJson(crmMessageFromEsbService.queryBopsPaymentList(baseRequestForBops));
	}

	/**
	 * 查询优惠券信息接口
	 * userIdList	List<String>	用户ID	不可以
	 * status	String	优惠券状态	可以
	 * 返回：
	 * {
	 * "couponTemplateModel": {
	 * "amount": 100,
	 * "campaignsRules": [
	 * {
	 * "codes": [],
	 * "selectAllFlag": true,
	 * "type": "discount"
	 * },
	 * {
	 * "codes": [],
	 * "selectAllFlag": true,
	 * "type": "fixed_price"
	 * },
	 * {
	 * "codes": [],
	 * "selectAllFlag": true,
	 * "type": "group"
	 * }
	 * ],
	 * "code": "HR5KNVA5",
	 * "couponType": "reduce",
	 * "creator": "jun.chen",
	 * "discount": null,
	 * "effectiveEndDate": null,
	 * "effectiveStartDate": null,
	 * "expiredDays": 5,
	 * "gmtCreate": "2020-04-29 15:39:38",
	 * "gmtModified": "2020-04-29 15:39:37",
	 * "goodsCodes": [
	 * "FindCM"
	 * ],
	 * "id": 44,
	 * "name": "高博专用券满100.01-100",
	 * "opUser": "jun.chen",
	 * "orderTypes": [
	 * "new",
	 * "renew",
	 * "expand"
	 * ],
	 * "reduceAmount": 100,
	 * "status": "enable",
	 * "validTimeType": "related"
	 * },
	 * "couponsNo": "CLFXTTX9TXVF",
	 * "effectiveTime": "2020-05-06 20:06:50",
	 * "expirationTime": "2020-05-11 20:06:50",
	 * "grantTime": "2020-05-06 20:06:50",
	 * "ownerId": 106755,
	 * "status": "to_use",
	 * "usedById": null,
	 * "usedTime": null
	 * }
	 */
	public void queryBopsCouponsList(BasePageRequest<IParamForBops> basePageRequest) {
		if (basePageRequest == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}

		IParamForBops param = basePageRequest.getData();
		if (param == null) {
			renderJson(R.error("请求参数不能为空"));
			return;
		}
		if (param.getUserIdList() == null || param.getUserIdList().size() == 0) {
			renderJson(R.error("请求参数:userIdList 不能为空"));
			return;
		}

		BaseRequestForBops.PageParamForBops pageParamForBops = new BaseRequestForBops.PageParamForBops();
		pageParamForBops.setPageNum(basePageRequest.getPage());
		pageParamForBops.setPageSize(basePageRequest.getLimit());

		BaseRequestForBops baseRequestForBops = new BaseRequestForBops();
		baseRequestForBops.setCaller(CrmConstant.CRM_SR_CODE);
		baseRequestForBops.setRequestId(IdUtil.simpleUUID());
		baseRequestForBops.setPaged(pageParamForBops);
		baseRequestForBops.setParam(basePageRequest.getData());

		JSONObject jsonObject = crmMessageFromEsbService.queryBopsCouponsList(baseRequestForBops);
		renderJson(jsonObject);
	}

	@NotNullValidate(value = "orderNo", message = "orderNo 不能为空")
	public void queryOrderDetail(@Para("orderNo") String orderNo) {
		renderJson(crmMessageFromEsbService.queryOrderDetail(orderNo));
	}

}
