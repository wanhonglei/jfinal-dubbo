package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.DistributorBdReport;
import com.kakarote.crm9.erp.crm.entity.DistributorBdReportResult;
import com.kakarote.crm9.erp.crm.entity.DistributorBdSalesQuantityReport;
import com.kakarote.crm9.erp.crm.entity.DistributorBdSalesQuantityReportResult;
import com.kakarote.crm9.erp.crm.entity.DistributorSaleAreaReport;
import com.kakarote.crm9.erp.crm.entity.DistributorSaleAreaReportResult;
import org.junit.Assert;
import org.junit.Test;

public class CrmDistributorReportServiceTest  extends BaseTest {

    private CrmDistributorReportService crmDistributorReportService = Aop.get(CrmDistributorReportService.class);
    @Test
    public void queryDistributorSaleAreaReportList() {


        String para = "{  \"SaleArea\": \"\",  \"ProductName\": \"\",  \"page\": 1,  \"limit\": 20,  \"deptId\":\"\",  \"saleAreaCode\":\"\",  \"productCode\":\"9ct1n9sk641599844\",  \"customerName\":\"fuhao\",  \"orderBys\":[{\"orderKey\":\"SUM_DELIVERY_QUANTITY\",\"orderType\":\"DESC\"},  {\"orderKey\":\"THEORETICAL_INVENTORY\",\"orderType\":\"ASC\"}]}";
        BasePageRequest<DistributorSaleAreaReport> basePageRequest = new BasePageRequest<DistributorSaleAreaReport>(para,DistributorSaleAreaReport.class);
        Page<DistributorSaleAreaReportResult> result = crmDistributorReportService.queryDistributorSaleAreaReportList(basePageRequest, 2736L, false);
        Assert.assertNotNull(result);
    }

    @Test
    public void queryDistributorBdReportList() {

        String para = "{  \"SaleArea\": \"\",  \"ProductName\": \"\",  \"page\": 1,  \"limit\": 20,  \"deptId\":\"\",  \"saleAreaCode\":\"\",  \"productCode\":\"9ct1n9sk641599844\",  \"customerName\":\"fuhao\",  \"orderBys\":[{\"orderKey\":\"SUM_DELIVERY_QUANTITY\",\"orderType\":\"DESC\"},  {\"orderKey\":\"THEORETICAL_INVENTORY\",\"orderType\":\"ASC\"}]}";
        BasePageRequest<DistributorBdReport> basePageRequest = new BasePageRequest<DistributorBdReport>(para,DistributorBdReport.class);
        Page<DistributorBdReportResult> result = crmDistributorReportService.queryDistributorBdReportList(basePageRequest, 2736L, false);
        Assert.assertNotNull(result);
    }

    @Test
    public void queryDistributorBdSalesQuantityReportList() {

        String para = "{  \"SaleArea\": \"\",  \"ProductName\": \"\",  \"page\": 1,  \"limit\": 20,  \"deptId\":\"\",  \"saleAreaCode\":\"\",  \"productCode\":\"9ct1n9sk641599844\",  \"customerName\":\"fuhao\",  \"orderBys\":[{\"orderKey\":\"SUM_DELIVERY_QUANTITY\",\"orderType\":\"DESC\"},  {\"orderKey\":\"THEORETICAL_INVENTORY\",\"orderType\":\"ASC\"}]}";
        BasePageRequest<DistributorBdSalesQuantityReport> basePageRequest = new BasePageRequest<DistributorBdSalesQuantityReport>(para,DistributorBdSalesQuantityReport.class);
        Page<DistributorBdSalesQuantityReportResult> result = crmDistributorReportService.queryDistributorBdSalesQuantityReportList(basePageRequest, 2736L, false);
        Assert.assertNotNull(result);
    }

    @Test
    public void queryDistributorProductList() {

        String para = "千寻";
        Page<Record> result = crmDistributorReportService.queryDistributorProductList(para, 2736L, false);
        Assert.assertNotNull(result);
    }

    @Test
    public void queryDistributorList() {

        String para = "清风";
        Page<Record> result = crmDistributorReportService.queryDistributorList(para, 2736L, false);
        Assert.assertNotNull(result);
    }
}
