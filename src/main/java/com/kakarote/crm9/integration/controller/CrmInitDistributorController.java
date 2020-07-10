package com.kakarote.crm9.integration.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.crm.service.CrmInitDistributorService;
import com.kakarote.crm9.utils.R;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CrmInitDistributorController.
 *
 * @author yue.li
 * @create 2020/04/20 10:00
 */
@Before(IocInterceptor.class)
public class CrmInitDistributorController extends Controller {

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Inject
    private CrmInitDistributorService crmInitDistributorService;

    @Autowired
    private NotifyService notifyService;

    private Log logger = Log.getLog(getClass());

    /**
     * 初始化全量分销商平台数据
     * @author yue.li
     */
    public void initDistributor() {
        try{
            crmInitDistributorService.initDistributor();
        }catch(Exception e) {
            // 发送失败消息通知
            adminSendEmailService.sendErrorMessage(e,notifyService);
        }
        renderJson(R.ok());
    }
}
