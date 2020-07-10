package com.kakarote.crm9.integration.controller;

import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmEmailConstant;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.kakarote.crm9.erp.crm.constant.CrmConstant.EMAIL_SUBJECT_PAYMENT;

/**
 * 库容溢出，发送邮件提示 定时任务
 * @author honglei.wan
 */
@Before(IocInterceptor.class)
@Slf4j
public class StorageOverFlowController extends Controller {

    @Autowired
    private NotifyService notifyService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Autowired
    private VelocityEngine velocityEngine;

    /**
     * 检查库容是否超过上限，并且进行提示
     */
    public void checkStorageAndPrompt() {
        log.info("checkStorageAndPrompt runTime: {}" ,LocalDate.now());

        List<Record> recordList = Db.find(Db.getSql("crm.userCapacity.findAllUserStorage"));
        recordList.forEach(o -> {
            String userName = o.getStr("realname");
            Integer relateCap = o.getInt("relate_cap");
            Integer usedRelateCap = o.getInt("used_relate_cap");
            String email = o.getStr("email");

            if (StringUtils.isBlank(email)){
                log.error("用户：{}，没有email",email);
                return;
            }

            Map<String, Object> params = Maps.newHashMap();
            params.put("userName", userName);
            params.put("relateCap", relateCap);
            params.put("usedRelateCap", usedRelateCap);

            StringWriter result = new StringWriter();
            VelocityContext velocityContext = new VelocityContext(params);
            velocityEngine.mergeTemplate("email/mail-storage-overflow.vm", "UTF-8", velocityContext, result);

            notifyService.email(CrmEmailConstant.STORAGE_OVERFLOW, result.toString() , Collections.singletonList(email));
        });

        renderJson(R.ok());
    }
}
