package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.CustomerReceivablesReport;
import org.junit.Test;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/12 8:11 下午
 */
public class CrmCustomerReceivablesReportServiceTest extends BaseTest {

	private CrmCustomerReceivablesReportService crmCustomerReceivablesReportService = Aop.get(CrmCustomerReceivablesReportService.class);

	@Test
	public void queryCustomerReceivablesReportList() {
		BasePageRequest<CustomerReceivablesReport> basePageRequest = new BasePageRequest<>(1,10,new CustomerReceivablesReport());
		Long userId = 1992L;
		super.mockBaseUtil("1011");
		crmCustomerReceivablesReportService.queryCustomerReceivablesReportList(basePageRequest,userId,false);
		crmCustomerReceivablesReportService.queryCustomerReceivablesReportList(basePageRequest,userId,true);
	}
}
