package com.kakarote.crm9.integration.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.utils.R;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * RedisResetController.
 *
 * @author yue.li
 * @create 2019/9/03 10:00
 */
@Before(IocInterceptor.class)
public class RedisResetController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Autowired
    private NotifyService notifyService;

    /**
     * 重新赋予网站客户池的redis
     * @author yue.li
     */
    public void resetRedis() {
        try{
            logger.info("resetRedis开始执行");
            crmCustomerService.redisReset();
            logger.info("resetRedis结束执行");
        }catch(Exception e){
            logger.error(String.format("resetRedis %s",e.getMessage()));
            /**发送失败消息通知*/
            adminSendEmailService.sendErrorMessage(e,notifyService);
        }
        renderJson(R.ok());
    }
}
