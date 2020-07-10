package com.kakarote.crm9.integration.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.CrmMqMessageService;
import com.qxwz.lyra.common.mq.consumer.LyraMqConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/17 4:24 下午
 */
@Slf4j(topic = "com.kakarote.crm9.integration.controller.MqMessageCronController")
public abstract class BaseMqConsumer implements LyraMqConsumer {
    protected CrmMqMessageService crmMqMessageService = Aop.get(CrmMqMessageService.class);

    /**
     * 是否立即消费
     */
    protected boolean consumeImmediately = true;

    @Override
    public void execute(MessageExt msg, Object obj) {
        log.info("msg consume msgId:{}", Objects.isNull(msg) ? null : msg.getMsgId());
        /* 校验消息体 */
        if (Objects.isNull(msg) || Objects.isNull(obj)) {
            log.error("msg consume fail: msg or content is null");
            return;
        }
        try {
            String tag = msg.getTags();
            log.info("msg consume tag:{},content:{}", tag, obj);
            JSONObject jsonO;
            if (obj instanceof String) {
                jsonO = JSON.parseObject((String) obj);
            } else {
                jsonO = (JSONObject) JSON.toJSON(obj);
            }
            /* 过滤消息 */
            if (!filter(jsonO)) {
                log.info("msg filtered.[msgId:{}]", msg.getMsgId());
                return;
            }
            if (consumeImmediately) {
                log.info("msg consume immediately.[msgId:{}]", msg.getMsgId());
                consumeImmediately(msg, jsonO);
            } else {
                log.info("msg consume late.[msgId:{}]", msg.getMsgId());
                consumeLate(msg, jsonO);
            }
            log.info("msg consume end msgId:{}", msg.getMsgId());
        } catch (Exception e) {
            log.error(String.format("msg consume fail.[msgId:%s]", msg.getMsgId()), e);
        }
    }

    /**
     * 立即消费
     *
     * @param msg
     * @param obj
     */
    private void consumeImmediately(MessageExt msg, JSONObject obj) {
        /* MQ消息插到库里 */
        MqMsg mqMsg = crmMqMessageService.saveMqMsg2Crm(msg, obj);
        if (Objects.isNull(mqMsg)) {
            log.info("msg consume skipped: duplicated msg[msgId:{}]", msg.getMsgId());
            return;
        }
        Db.tx(() -> {
            /* 实时消费 */
            if (consume(mqMsg)) {
                /* 更新消息消费状态 */
                crmMqMessageService.updateConsumeStatus(mqMsg.getId());
            }
            return true;
        });
    }

    /**
     * 延迟消费
     *
     * @param msg
     * @param obj
     */
    private void consumeLate(MessageExt msg, JSONObject obj) {
        Db.tx(() -> {
            /* MQ消息插到库里 */
            MqMsg mqMsg = crmMqMessageService.saveMqMsg2Crm(msg, obj);
            if (Objects.isNull(mqMsg)) {
                log.info("msg consume skipped: duplicated msg[msgId:{}]", msg.getMsgId());
            }
            return true;
        });
    }

    /**
     * 消费消息列表
     *
     * @param msgs
     */
    public void consume(List<MqMsg> msgs) {
        if (CollectionUtils.isEmpty(msgs)) {
            return;
        }
        Db.tx(() -> {
            msgs.forEach(msg -> {
                //消费MQ消息
                if (consume(msg)) {
                    //更新消息消费状态
                    crmMqMessageService.updateConsumeStatus(msg.getId());
                }
            });
            return true;
        });
    }

    /**
     * 过滤判断
     *
     * @param obj
     * @return
     */
    protected boolean filter(JSONObject obj) {
        return true;
    }

    /**
     * 消费MQ消息
     *
     * @param msg
     * @return true 消息会被更新为消费成功
     */
    protected abstract boolean consume(MqMsg msg);
}
