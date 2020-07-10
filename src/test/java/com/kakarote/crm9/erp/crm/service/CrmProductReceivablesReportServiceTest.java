package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmProductReceivablesReport;
import org.junit.Test;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/22 11:33 上午
 */
public class CrmProductReceivablesReportServiceTest extends BaseTest {

    private CrmProductReceivablesReportService crmProductReceivablesReportService = Aop.get(CrmProductReceivablesReportService.class);

    @Test
    public void queryProductReceivablesReportList() {
        mockBaseUtil("1011");
        String data = "{}";
        crmProductReceivablesReportService.queryProductReceivablesReportList(new BasePageRequest<>(data, CrmProductReceivablesReport.class), 1992L, true);
        crmProductReceivablesReportService.queryProductReceivablesReportList(new BasePageRequest<>(data, CrmProductReceivablesReport.class), 1992L, false);
    }
}