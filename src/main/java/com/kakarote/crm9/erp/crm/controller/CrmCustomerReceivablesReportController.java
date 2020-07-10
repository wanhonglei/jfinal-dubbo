package com.kakarote.crm9.erp.crm.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.common.CrmCustomerReceivablesReportEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CustomerReceivablesReport;
import com.kakarote.crm9.erp.crm.entity.CustomerReceivablesReportResult;
import com.kakarote.crm9.erp.crm.service.CrmCustomerReceivablesReportService;
import com.kakarote.crm9.utils.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 客户回款报表
 * @author xiaowen.wu
 *
 */
public class CrmCustomerReceivablesReportController extends Controller {

	@Inject
	private CrmCustomerReceivablesReportService crmCustomerReceivablesReportService;
	
    private Log logger = Log.getLog(getClass());

	@Permissions("board:customerReceivables:view")
	public void queryCustomerReceivablesReportList(BasePageRequest<CustomerReceivablesReport> basePageRequest) {
		try {
			renderJson(R.ok().put("data", crmCustomerReceivablesReportService.queryCustomerReceivablesReportList(basePageRequest, BaseUtil.getUserId(), true)));
		} catch (Exception e) {
			logger.error(String.format("queryCustomerReceivablesReportList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}

	/**
	 * 客户回款报表导出
	 * @author yue.li
	 * return
	 */
	@Permissions("board:customerReceivables:export")
	public void exportCustomerReceivablesReportExcel(BasePageRequest<CustomerReceivablesReport> basePageRequest) throws Exception{
		CustomerReceivablesReportResult customerReceivablesReportResult= crmCustomerReceivablesReportService.queryCustomerReceivablesReportList(basePageRequest, BaseUtil.getUserId(), false);
        if(customerReceivablesReportResult.getPageResult().getList() != null && customerReceivablesReportResult.getPageResult().getList().size() >0) {
            for(Record record : customerReceivablesReportResult.getPageResult().getList()) {
                record.set("receivablesPlanTime", CrmDateUtil.formatDate(record.getStr("receivablesPlanTime")));
                record.set("receivablesTime",CrmDateUtil.formatDate(record.getStr("receivablesTime")));
            }
        }
		List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
		List<List<Record>> resultAllList = new ArrayList<>();
		headAllList.add(initMain());
		resultAllList.add(customerReceivablesReportResult.getPageResult().getList());
		ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.CUSTOMER_RECEIVABLES_REPORT,getResponse(),null);
		renderNull();
	}

	/***
	 * 客户回款报表导出head
	 * @author yue.li
	 */
	public LinkedHashMap<String,String> initMain() {
		LinkedHashMap<String,String> headList = new LinkedHashMap<>();
		for(CrmCustomerReceivablesReportEnum customerReceivableEnum : CrmCustomerReceivablesReportEnum.values()) {
			headList.put(customerReceivableEnum.getTypes(),customerReceivableEnum.getName());
		}
		return headList;
	}
	
	public void getCustomerReceivablesReportDeptList() {
        try{
            renderJson(R.ok().put("data",CrmReportDeptUtil.getCrmReportDeptList()));
        }catch (Exception e){
            logger.error(String.format("getCustomerReceivablesReportDeptList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
	
}
