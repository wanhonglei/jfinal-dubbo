package com.kakarote.crm9.integration.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.CrmMqMessageService;
import com.kakarote.crm9.integration.mq.BopsAbnormalFundsConsumer;
import com.kakarote.crm9.integration.mq.DistributorBindConsumer;
import com.kakarote.crm9.integration.mq.DistributorFreezeConsumer;
import com.kakarote.crm9.integration.mq.DistributorUnbindConsumer;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/27 11:35 上午
 */
@Slf4j(topic = "com.kakarote.crm9.integration.controller.MqMessageCronController")
@Before(IocInterceptor.class)
public class MqDistributorController extends Controller {
    @Autowired
    private NotifyService notifyService;

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Autowired
    private DistributorBindConsumer distributorBindConsumer;

    @Autowired
    private DistributorUnbindConsumer distributorUnbindConsumer;

    @Autowired
    private DistributorFreezeConsumer distributorFreezeConsumer;

    @Autowired
    private BopsAbnormalFundsConsumer bopsAbnormalFundsConsumer;

    @Inject
    private CrmMqMessageService mqMessageService;

    public void handleMsg() {
        try {
            log.info("MqDistributorController handleMsg start");
            /* 分销商绑定消息 */
            List<MqMsg> bindMsgs = mqMessageService.findUnConsumedMessageByTopicAndTags(distributorBindConsumer.topic(), distributorBindConsumer.tags());
            distributorBindConsumer.consume(bindMsgs);
            log.info("MqDistributorController distributorBindMsg consume count {} ", Objects.isNull(bindMsgs) ? 0 : bindMsgs.size());

            /* 分销商解绑消息 */
            List<MqMsg> unBindMsgs = mqMessageService.findUnConsumedMessageByTopicAndTags(distributorUnbindConsumer.topic(), distributorUnbindConsumer.tags());
            distributorUnbindConsumer.consume(unBindMsgs);
            log.info("MqDistributorController distributorUnBindMsg consume count {} ", Objects.isNull(unBindMsgs) ? 0 : unBindMsgs.size());

            /* 分销商冻结解冻消息 */
            List<MqMsg> freezeMsgs = mqMessageService.findUnConsumedMessageByTopicAndTags(distributorFreezeConsumer.topic(), distributorFreezeConsumer.tags());
            distributorFreezeConsumer.consume(freezeMsgs);
            log.info("MqDistributorController distributorFreezeMsg consume count {} ", Objects.isNull(freezeMsgs) ? 0 : freezeMsgs.size());

            /* 异常资金消息 */
            List<MqMsg> abnormalFundsMsgs = mqMessageService.findUnConsumedMessageByTopicAndTags(bopsAbnormalFundsConsumer.topic(), bopsAbnormalFundsConsumer.tags());
            bopsAbnormalFundsConsumer.consume(abnormalFundsMsgs);
            log.info("MqDistributorController abnormalFundsMsgs consume count {} ", Objects.isNull(abnormalFundsMsgs) ? 0 : abnormalFundsMsgs.size());

            renderJson(R.ok());
        } catch (Exception e) {
            log.error("MqDistributorController -> handleMsg -> 处理异常", e);
            adminSendEmailService.sendErrorMessage(e, notifyService);
            renderJson(R.error("MqDistributorController -> handleMsg -> 处理异常"));
        }
    }

}
