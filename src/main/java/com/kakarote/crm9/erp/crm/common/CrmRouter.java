package com.kakarote.crm9.erp.crm.common;

import com.jfinal.config.Routes;
import com.kakarote.crm9.common.interceptor.AuthInterceptor;
import com.kakarote.crm9.common.interceptor.CrmEventInterceptor;
import com.kakarote.crm9.common.interceptor.LogApiOperationInterceptor;
import com.kakarote.crm9.erp.crm.aliyun.controller.CccController;
import com.kakarote.crm9.erp.crm.controller.*;

public class CrmRouter extends Routes {
    @Override
    public void config() {
        addInterceptor(new CrmInterceptor());
        addInterceptor(new AuthInterceptor());
        addInterceptor(new LogApiOperationInterceptor());
        addInterceptor(new CrmEventInterceptor());
        add("/crm/comment", CommentController.class);

        add("/crm/CrmProduct", CrmProductController.class);
        add("/crm/CrmProductCategory", CrmProductCategoryController.class);
        add("/crm/CrmLeads", CrmLeadsController.class);
        add("/crm/CrmCustomer", CrmCustomerController.class);
        add("/crm/CrmBusiness", CrmBusinessController.class);
        add("/crm/CrmContacts", CrmContactsController.class);
        add("/crm/contract", CrmContractController.class);
        add("/crm/CrmReceivables", CrmReceivablesController.class);
        add("/crm/CrmRecord",CrmRecordController.class);
        add("/crm/Crm/ReceivablesPlan",CrmReceivablesPlanController.class);
        add("/crm/CrmBackLog",CrmBackLogController.class);
        add("/crm/CrmPlanReport",CrmPlanReportController.class);
        add("/crm/CrmSaleUsualReport",CrmSaleUsualReportController.class);
        add("/crm/CrmCustomerReceivablesReport",CrmCustomerReceivablesReportController.class);
        add("/crm/CrmDepartmentIncomeReport",CrmDepartmentIncomeReportController.class);
        add("/crm/CrmProductReceivablesReport",CrmProductReceivablesReportController.class);
        add("/crm/CrmPayment", CrmPaymentController.class);
        add("/crm/CrmDeliveryInformation", CrmDeliveryInformationController.class);
        add("/crm/CrmDistributorReport", CrmDistributorReportController.class);
        add("/crm/CrmSignin", CrmSignInController.class);
        add("/crm/Notes", CrmNotesController.class);
        add("/crm/aliyun/ccc/api", CccController.class);
        add("/crm/contactRecord", CrmContactRecordController.class);
        add("/crm/messageFromEsb", CrmMessageFromEsbController.class);
        add("/crm/businessReport", CrmBusinessReportController.class);
    }
}
