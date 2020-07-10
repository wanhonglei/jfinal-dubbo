package com.kakarote.crm9.integration.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.erp.crm.service.CrmMqMessageService;
import com.kakarote.crm9.utils.BaseUtil;
import com.qxwz.lyra.common.mq.consumer.LyraMqConsumer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Site Member Auth Change Consumer
 *
 * @author hao.fu
 * @since 2019/7/5 9:21
 */
public class SiteMemberAuthChangeConsumer implements LyraMqConsumer {

    private Log logger = Log.getLog(getClass());

    private String TOPIC_MEMBER_AUDIT_INFO_CHANGE;
    private String TAG_PERSONAL_INFO_CHANGE;
    private NotifyService notifyService;
    private String warningEmailTopic;
    private String warningEmailToList;

    private CrmMqMessageService mqMessageService = Aop.get(CrmMqMessageService.class);

    @Override
    public String topic() {
        return TOPIC_MEMBER_AUDIT_INFO_CHANGE;
    }

    @Override
    public List<String> tags() {
        List<String> tags = Lists.newArrayList();
        tags.add(TAG_PERSONAL_INFO_CHANGE);
        return tags;
    }

    @Override
    public void execute(MessageExt msg, Object obj) {
        try {
            if (Objects.nonNull(msg) && Objects.nonNull(obj)) {
                String tag = msg.getTags();
                logger.info(String.format("#############SiteMemberAuthChangeConsumer ---> tag is: %s", tag));
                if (TAG_PERSONAL_INFO_CHANGE.equals(tag)) {
                    logger.info(String.format("#############person data: msg is: %s, obj is: %s", msg.toString(), obj.toString()));
                    if (obj instanceof String) {
                        obj = JSON.parseObject(obj.toString());
                    }
                    mqMessageService.saveMqMsg2Crm(msg, obj);
                }
            }
        } catch (Exception e) {
            String ex = BaseUtil.getExceptionStack(e);
            logger.error("#############SiteMemberAuthChangeConsumer has exception:" + ex);
            if (warningEmailToList != null && warningEmailToList.length() > 0) {
                List<String> toList = Arrays.asList(warningEmailToList.split(","));
                notifyService.email(warningEmailTopic, ex, toList);
            }
        }
    }

    public String getTOPIC_MEMBER_AUDIT_INFO_CHANGE() {
        return TOPIC_MEMBER_AUDIT_INFO_CHANGE;
    }

    public void setTOPIC_MEMBER_AUDIT_INFO_CHANGE(String TOPIC_MEMBER_AUDIT_INFO_CHANGE) {
        this.TOPIC_MEMBER_AUDIT_INFO_CHANGE = TOPIC_MEMBER_AUDIT_INFO_CHANGE;
    }

    public String getTAG_PERSONAL_INFO_CHANGE() {
        return TAG_PERSONAL_INFO_CHANGE;
    }

    public void setTAG_PERSONAL_INFO_CHANGE(String TAG_PERSONAL_INFO_CHANGE) {
        this.TAG_PERSONAL_INFO_CHANGE = TAG_PERSONAL_INFO_CHANGE;
    }

    public NotifyService getNotifyService() {
        return notifyService;
    }

    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    public String getWarningEmailTopic() {
        return warningEmailTopic;
    }

    public void setWarningEmailTopic(String warningEmailTopic) {
        this.warningEmailTopic = warningEmailTopic;
    }

    public String getWarningEmailToList() {
        return warningEmailToList;
    }

    public void setWarningEmailToList(String warningEmailToList) {
        this.warningEmailToList = warningEmailToList;
    }
}
