package com.kakarote.crm9.erp.admin.common;

import com.jfinal.config.Routes;
import com.kakarote.crm9.common.interceptor.AuthInterceptor;
import com.kakarote.crm9.common.interceptor.LogApiOperationInterceptor;
import com.kakarote.crm9.erp.admin.controller.*;

public class AdminRouter extends Routes {
    @Override
    public void config() {
        addInterceptor(new AdminInterceptor());
        addInterceptor(new AuthInterceptor());
        addInterceptor(new LogApiOperationInterceptor());
        add("/crm", AdminLoginController.class);
        add("/crm/system/user", AdminUserController.class);
        add("/crm/system/dept", AdminDeptController.class);
        add("/crm/system/menu", AdminMenuController.class);
        add("/crm/system/role", AdminRoleController.class);
        add("/crm/file", AdminFileController.class);
        add("/crm/field",AdminFieldController.class);
        add("/crm/scene",AdminSceneController.class);
        add("/crm/businessType", AdminBusinessTypeController.class);
        add("/crm/achievement",AdminAchievementController.class);
        add("/crm/scenario", AdminScenarioController.class);
        add("/crm/common", AdminDataDicController.class);
        add("/crm/businessStatusType", AdminBusinessStatusTypeController.class);
        //部门归属用户行业类型
        add("/crm/industry", AdminIndustryOfDeptController.class);
        add("/crm/adminConfig", AdminConfigController.class);
        add("/crm/customerReceiveRole", AdminCustomerReceiveRoleController.class);
        //商机组，商机阶段
        add("/crm/crmBusinessGroup", AdminBusinessGroupController.class);
        add("/crm/crmBusinessStatus", AdminBusinessStatusController.class);
        //库容改造
        add("/crm/capacity", AdminCapacityController.class);
    }
}
