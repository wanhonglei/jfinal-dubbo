package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.BaseTest;
import org.junit.Test;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/15 11:57 上午
 */
public class CrmMessageFromEsbServiceTest extends BaseTest {

	private CrmMessageFromEsbService crmMessageFromEsbService = new CrmMessageFromEsbService();

	@Test
	public void queryOrderList() {
		try {
			crmMessageFromEsbService.queryOrderList(null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	@Test
	public void queryInstanceList() {
		try {
			crmMessageFromEsbService.queryInstanceList(null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryOrderSummaryList() {
		try {
			crmMessageFromEsbService.queryOrderSummaryList(null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryBopsInvoiceList() {
		try {
			crmMessageFromEsbService.queryBopsInvoiceList(null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryBopsInvoiceTotalAmountList() {
		try {
			crmMessageFromEsbService.queryBopsInvoiceTotalAmountList(null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryBopsRedeemCodeList() {
		try {
			crmMessageFromEsbService.queryBopsRedeemCodeList(null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryBopsPaymentList() {
		try {
			crmMessageFromEsbService.queryBopsPaymentList(null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryBopsCouponsList() {
		try {
			crmMessageFromEsbService.queryBopsCouponsList(null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void transferContractPaymentToAgreement() {
		try {
			crmMessageFromEsbService.transferContractPaymentToAgreement(new JSONObject());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}