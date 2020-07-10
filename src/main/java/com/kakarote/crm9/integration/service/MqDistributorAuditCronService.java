package com.kakarote.crm9.integration.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmMqMessageService;
import com.kakarote.crm9.erp.crm.service.handler.customer.sync.BaseSyncHandler;
import com.kakarote.crm9.erp.crm.service.handler.customer.sync.CustomerSyncHandler;
import com.kakarote.crm9.integration.entity.DistributorAuditDTO;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 处理分销商消息service
 *
 * @author xiaowen.wu
 */
@Slf4j
public class MqDistributorAuditCronService {

    private Log logger = Log.getLog(getClass());

    public static final String TOPIC = JfinalConfig.crmProp.get("backend.distributor.audit.topic");

    public static final String AUDIT_TAG = JfinalConfig.crmProp.get("backend.distributor.audit.success.tag");

    private static final String WARNING_EMAIL_TOPIC = JfinalConfig.crmProp.get("mq.warning.email.topic");
    private static final String WARNING_EMAIL_TO_LIST = JfinalConfig.crmProp.get("mq.warning.email.tolist");

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmMqMessageService crmMqMessageService;

    @Inject
    private SiteMemberMqMessageService siteMemberMqMessageService;

    private AdminDataDicService adminDataDicService = Aop.get(AdminDataDicService.class);

    @Autowired
    private NotifyService notifyService;

    /**
     * Handle MQ message in CRM DB.
     */

    public R handleMsg() {
        // 获取所有未处理的mq消息
        try {
            List<Record> records = crmMqMessageService.getUnconsumedMessage();
            if (CollectionUtils.isNotEmpty(records)) {
                List<MqMsg> msgs = records.stream().map(item -> new MqMsg()._setAttrs(item.getColumns())).collect(Collectors.toList());

                // 通过mq tag筛选分销商认证信息
                List<MqMsg> distributorMsg = msgs.stream().filter(item -> AUDIT_TAG.equals(item.getTag())).collect(Collectors.toList());
                // 处理分销商mq消息
                handleDistributorMessage(distributorMsg);
            }
        } catch (Exception e) {
            logger.error("handleMsg -> 分销商认证处理异常:" + BaseUtil.getExceptionStack(e));
        }
        return R.ok();
    }

    /**
     * 处理分销商消息
     *
     * @param mqMsgs
     */
    private void handleDistributorMessage(List<MqMsg> mqMsgs) {
        if (CollectionUtils.isNotEmpty(mqMsgs)) {
            logger.info(String.format("MqDistributorAuditCronController -> handleDistributorMessage -> auth success msg count: %d", mqMsgs.size()));

            List<String> errorMsgs = Lists.newArrayList();
            for (MqMsg item : mqMsgs) {
                logger.info(String.format("MqDistributorAuditCronController -> handleDistributorMessage -> tag is: %s, msg is: %s", item.getTag(), item.getContent()));
                // 分销商认证成功信息
                if (AUDIT_TAG.equals(item.getTag())) {
                    try {
                        handleDistributorAudit(item);
                    } catch (Exception e) {
                        // 出现异常打印日志，继续执行下一条
                        logger.error("handleDistributorMessage -> 分销商认证处理异常:72crm_mq_msg.id :" + item.getId() + "msg :\\r\\n" + BaseUtil.getExceptionStack(e));
                        errorMsgs.add("分销商认证处理异常：72crm_mq_msg.id :" + item.getId() + "msg :\\r\\n" + BaseUtil.getExceptionStack(e));
                    }
                }
            }
            // 批量发送错误邮件
            if (CollectionUtils.isNotEmpty(errorMsgs) && Objects.nonNull(WARNING_EMAIL_TO_LIST)) {
                StringBuffer errorMsgStr = new StringBuffer();
                errorMsgs.forEach(errorMsg -> errorMsgStr.append(errorMsg).append("\\r\\n"));
                notifyService.email(WARNING_EMAIL_TOPIC, errorMsgStr.toString(), Arrays.asList(WARNING_EMAIL_TO_LIST.split(",")));
            }
        }
    }

    /**
     * 处理分销商消息
     *
     * @param item
     */
    private void handleDistributorAudit(MqMsg item) {
        logger.info(String.format("handleDistributorAudit mqMsg content: %s", item.getContent()));
        DistributorAuditDTO distributorAuditDTO = JSON.parseObject(item.getContent(), DistributorAuditDTO.class);

        Db.tx(() -> {
            BaseSyncHandler handler = CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Distributor_Audit);
            handler.handle(distributorAuditDTO);

            // 保证网站客户创建消息先消费成功，才消费分销商认证消息
            crmMqMessageService.updateConsumeStatus(item.getId());
            return true;
        });
    }
}
