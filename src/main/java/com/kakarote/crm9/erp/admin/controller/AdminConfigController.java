package com.kakarote.crm9.erp.admin.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.service.AdminConfigService;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminConfigController
 *
 * @author yue.li
 * @date 2020/03/10
 */
@Before(IocInterceptor.class)
public class AdminConfigController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminConfigService adminConfigService;

    /**
     * 网站客户保护规则设置
     * @author yue.li
     */
    @NotNullValidate(value = "day", message = "网站客户保护时间为空")
    public void websiteCustomerPoolSetting() {
        try {
            renderJson(adminConfigService.updateConfigByName(CrmConstant.WEBSITE_CUSTOMER_POOL_SETTING,getPara("day")));
        } catch (Exception e) {
            logger.error(String.format("websiteCustomerPoolSetting msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 到单客户领取规则设置
     * @author yue.li
     */
    @NotNullValidate(value = "websitePerformanceValue", message = "网站业绩计入值为空")
    @NotNullValidate(value = "targetDeptPerformanceValue", message = "目标部门业绩增加值为空")
    public void performanceSetting() {
        try {
            List<AdminConfig> adminConfigList = new ArrayList<>();

            String websitePerformanceValue = getPara("websitePerformanceValue");
            String targetDeptPerformanceValue = getPara("targetDeptPerformanceValue");

            adminConfigList.add(constructAdminConfig(CrmConstant.WEBSITE_PERFORMANCE_INCLUDED,websitePerformanceValue));
            adminConfigList.add(constructAdminConfig(CrmConstant.TARGET_DEPT_PERFORMANCE_INCLUDED,targetDeptPerformanceValue));

            renderJson(adminConfigService.updateBatchConfigByName(adminConfigList));
        } catch (Exception e) {
            logger.error(String.format("performanceSetting msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 构造adminConfig对象
     * @author yue.li
     * @param name 名称
     * @param value 值
     */
    private AdminConfig constructAdminConfig(String name,String value) {
        AdminConfig adminConfig = new AdminConfig();
        adminConfig.setName(name);
        adminConfig.setValue(value);
        return adminConfig;
    }

    /**
     * 查询网站客户保护期设置
     * @author yue.li
     */
    public void queryWebsiteCustomerPoolSetting() {
        try{
            renderJson(R.ok().put("data", adminConfigService.queryAdminConfig(CrmConstant.WEBSITE_CUSTOMER_POOL_SETTING)));
        }catch (Exception e){
            logger.error(String.format("queryWebsiteCustomerPoolSetting msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询到单客户领取规则设置
     * @author yue.li
     */
    public void queryCustomerReceivingSetting() {
        try{
            renderJson(R.ok().put("data", adminConfigService.queryCustomerReceivingSetting()));
        }catch (Exception e){
            logger.error(String.format("queryCustomerReceivingSetting msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}
