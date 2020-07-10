package com.kakarote.crm9.integration.common;

import com.jfinal.config.Routes;
import com.kakarote.crm9.common.interceptor.AuthInterceptor;
import com.kakarote.crm9.integration.controller.BopsPaymentInfoController;
import com.kakarote.crm9.integration.controller.CrmCustomerAutoReleaseController;
import com.kakarote.crm9.integration.controller.CrmDealLeadsCustomerController;
import com.kakarote.crm9.integration.controller.CrmInitDistributorController;
import com.kakarote.crm9.integration.controller.CrmInterfaceForEsbController;
import com.kakarote.crm9.integration.controller.CrmLeadsCronController;
import com.kakarote.crm9.integration.controller.CrmTemporaryController;
import com.kakarote.crm9.integration.controller.CrmTimeTaskController;
import com.kakarote.crm9.integration.controller.ExternalSystemInfoController;
import com.kakarote.crm9.integration.controller.LeadsAllocationController;
import com.kakarote.crm9.integration.controller.MqDistributorAuditCronController;
import com.kakarote.crm9.integration.controller.MqDistributorController;
import com.kakarote.crm9.integration.controller.MqMessageCronController;
import com.kakarote.crm9.integration.controller.RedisResetController;
import com.kakarote.crm9.integration.controller.SendMsgRecePlanController;
import com.kakarote.crm9.integration.controller.StorageOverFlowController;
import com.kakarote.crm9.integration.controller.SyncDepartmentController;
import com.kakarote.crm9.integration.controller.SyncStaffController;

/**
 * Integration router.
 *
 * @author hao.fu
 * @create 2019/6/26 14:45
 */
public class IntegrationRouter extends Routes {

    @Override
    public void config() {
        addInterceptor(new IntegrationInterceptor());
        addInterceptor(new AuthInterceptor());
        add("/crm/integration/staffs", SyncStaffController.class);
        add("/crm/integration/departments", SyncDepartmentController.class);
        add("/crm/integration/crmLeads", CrmLeadsCronController.class);
        add("/crm/integration/crmInterface", CrmInterfaceForEsbController.class);
        add("/crm/integration/endRecePlan", SendMsgRecePlanController.class);
        add("/crm/integration/mq", MqMessageCronController.class);
        add("/crm/integration/redis", RedisResetController.class);
        add("/crm/integration/bops", BopsPaymentInfoController.class);
        add("/crm/integration/externalSystemInfo", ExternalSystemInfoController.class);
        add("/crm/integration/mqDistributor", MqDistributorAuditCronController.class);
        add("/crm/integration/distributor", MqDistributorController.class);
        add("/crm/integration/dealLeadsCustomer", CrmDealLeadsCustomerController.class);
        add("/crm/integration/leadsAllocation", LeadsAllocationController.class);
        add("/crm/integration/customerRelease", CrmCustomerAutoReleaseController.class);
        add("/crm/integration/storageOverFlow", StorageOverFlowController.class);
        add("/crm/integration/initDistributor", CrmInitDistributorController.class);
        add("/crm/integration/temporary", CrmTemporaryController.class);
        add("/crm/integration/timeTask", CrmTimeTaskController.class);
    }
}
