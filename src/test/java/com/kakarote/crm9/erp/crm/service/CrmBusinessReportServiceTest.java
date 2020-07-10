package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessReport;
import org.junit.Test;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/26 1:43 下午
 */
public class CrmBusinessReportServiceTest extends BaseTest {

	private CrmBusinessReportService crmBusinessReportService = Aop.get(CrmBusinessReportService.class);

	@Test
	public void queryBusinessReportList() {
		CrmBusinessReport paramBean = new CrmBusinessReport();
//		paramBean.setBusinessName("人");
//		paramBean.setCustomerName("人");
		paramBean.setShareholderRelationCode("1,2");
//		paramBean.setCreateStartTime("2020-01-01");
//		paramBean.setCreateEndTime("2020-10-01");
//		paramBean.setDeptId(373);
		paramBean.setOrderKey("customerName");
		paramBean.setOrderType("desc");

		BasePageRequest<CrmBusinessReport> basePageRequest = new BasePageRequest<>(1,10,paramBean);
		Page<Record> reportList = crmBusinessReportService.queryBusinessReportList(basePageRequest);
		System.out.println(reportList);
	}
}