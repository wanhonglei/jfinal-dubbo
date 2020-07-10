package com.kakarote.crm9.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.integration.service.MqDistributorAuditCronService;
import com.kakarote.crm9.utils.R;

/**
 * 分销商消息批处理入口
 * @author xiaowen.wu
 *
 */
@Before(IocInterceptor.class)
public class MqDistributorAuditCronController  extends Controller {

    private Log logger = Log.getLog(MqMessageCronController.class);

    @Autowired
    private NotifyService notifyService;

    @Inject
    private AdminSendEmailService adminSendEmailService;
    
    @Inject
    MqDistributorAuditCronService mqDistributorAuditCronService;

    /**
     * Handle MQ message in CRM DB.
     */
    public void handleMsg() {
        try {
        	// 处理分销商认证信息
			renderJson(mqDistributorAuditCronService.handleMsg());
		} catch (Exception e) {
			logger.error("MqDistributorAuditCronController -> handleMsg -> 处理异常",e);
            adminSendEmailService.sendErrorMessage(e, notifyService);
			renderJson(R.error("MqDistributorAuditCronController -> handleMsg -> 处理异常"));
		}
    }
        
}
