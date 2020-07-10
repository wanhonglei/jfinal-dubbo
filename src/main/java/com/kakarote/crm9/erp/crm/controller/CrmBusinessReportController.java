package com.kakarote.crm9.erp.crm.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessReport;
import com.kakarote.crm9.erp.crm.service.CrmBusinessReportService;
import com.kakarote.crm9.utils.R;

/**
 * CRM 商机报表
 * @author honglei.wan
 */
public class CrmBusinessReportController extends Controller {

    @Inject
    private CrmBusinessReportService crmBusinessReportService;

    /**
     * 商机统计报表
     * @param basePageRequest
     */
    @LogApiOperation
    @Permissions("board:business_statistics_report:view")
    public void queryBusinessReportList(BasePageRequest<CrmBusinessReport> basePageRequest) {
        renderJson(R.ok().put("data", crmBusinessReportService.queryBusinessReportList(basePageRequest)));
    }

}