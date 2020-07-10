package com.kakarote.crm9.erp.crm.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.common.CrmProductReceivablesReportEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmProductReceivablesReport;
import com.kakarote.crm9.erp.crm.entity.CrmProductReceivablesReportResult;
import com.kakarote.crm9.erp.crm.service.CrmProductReceivablesReportService;
import com.kakarote.crm9.utils.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 商品回款报表
 * @author xiaowen.wu
 *
 */
public class CrmProductReceivablesReportController extends Controller {

	@Inject
	private CrmProductReceivablesReportService crmProductReceivablesReportService;
	
    private Log logger = Log.getLog(getClass());

	@Permissions("board:productReceivables:view")
	public void queryProductReceivablesReportList(BasePageRequest<CrmProductReceivablesReport> basePageRequest) {
		try {
			renderJson(R.ok().put("data", crmProductReceivablesReportService.queryProductReceivablesReportList(basePageRequest, BaseUtil.getUserId(), true)));
		} catch (Exception e) {
			logger.error(String.format("queryProductReceivablesReportList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
		}
	}

	/**
	 * 商品回款报表导出
	 * @author yue.li
	 * return
	 */
	@Permissions("board:productReceivables:export")
	public void exportProductReceivablesReportExcel(BasePageRequest<CrmProductReceivablesReport> basePageRequest) throws Exception{
	    CrmProductReceivablesReportResult productReceivablesReportResult= crmProductReceivablesReportService.queryProductReceivablesReportList(basePageRequest, BaseUtil.getUserId(), false);
		List<LinkedHashMap<String,String>> headAllList = new ArrayList<>();
		List<List<Record>> resultAllList = new ArrayList<>();
		headAllList.add(initMain());
        for(Record record : productReceivablesReportResult.getPageResult().getList()) {
            record.set("receivablesPlanTime", CrmDateUtil.formatDate(record.getStr("receivablesPlanTime")));
            record.set("receivablesTime",CrmDateUtil.formatDate(record.getStr("receivablesTime")));
        }
		resultAllList.add(productReceivablesReportResult.getPageResult().getList());
		ExcelExportUtil.export(headAllList,resultAllList, CrmConstant.PRODUCT_RECEIVABLES_REPORT,getResponse(),null);
		renderNull();
	}

	/***
	 * 商品回款报表导出head
	 * @author yue.li
	 */
	public LinkedHashMap<String,String> initMain() {
		LinkedHashMap<String,String> headList = new LinkedHashMap<>();
		for(CrmProductReceivablesReportEnum crmProductReceivablesReportEnum : CrmProductReceivablesReportEnum.values()) {
			headList.put(crmProductReceivablesReportEnum.getTypes(),crmProductReceivablesReportEnum.getName());
		}
		return headList;
	}

    /**
     * 获取商品回款收入对应的部门
     * @author yue.li
     * return
     */
    public void getProductReceivablesReportDeptList() {
        try{
            renderJson(R.ok().put("data", CrmReportDeptUtil.getCrmReportDeptList()));
        }catch (Exception e){
            logger.error(String.format("getProductReceivablesReportDeptList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
	
}
