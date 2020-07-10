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
 * Site Member Cancel Consumer
 *
 * @author hao.fu
 * @since 2019/7/5 9:26
 */
public class SiteUserCancelConsumer implements LyraMqConsumer {

    private Log logger = Log.getLog(getClass());

    private String TOPIC_MEMBER_CANCEL;
    private String TAG_MEMBER_CANCEL;
    private NotifyService notifyService;
    private String warningEmailTopic;
    private String warningEmailToList;

    CrmMqMessageService mqMessageService = Aop.get(CrmMqMessageService.class);

    @Override
    public String topic() {
        return TOPIC_MEMBER_CANCEL;
    }

    @Override
    public List<String> tags() {
        List<String> tags = Lists.newArrayList();
        tags.add(TAG_MEMBER_CANCEL);
        return tags;
    }

    @Override
    public void execute(MessageExt msg, Object obj) {
        try {
            if (Objects.nonNull(msg) && Objects.nonNull(obj)) {
                String tag = msg.getTags();
                logger.info(String.format("#############SiteUserCancelConsumer ---> tag is: %s", tag));
                if (TAG_MEMBER_CANCEL.equals(tag)) {
                    logger.info(String.format("#############member cancel data: msg is: %s, obj is: %s", msg.toString(), obj.toString()));
                    if (obj instanceof String) {
                        obj = JSON.parseObject(obj.toString());
                    }
                    mqMessageService.saveMqMsg2Crm(msg, obj);
                }
            }
        } catch (Exception e) {
            String ex = BaseUtil.getExceptionStack(e);
            logger.error("#############SiteUserCancelConsumer has exception:" + ex);
            if (warningEmailToList != null && warningEmailToList.length() > 0) {
                List<String> toList = Arrays.asList(warningEmailToList.split(","));
                notifyService.email(warningEmailTopic, ex, toList);
            }
        }
    }

    public String getTOPIC_MEMBER_CANCEL() {
        return TOPIC_MEMBER_CANCEL;
    }

    public void setTOPIC_MEMBER_CANCEL(String TOPIC_MEMBER_CANCEL) {
        this.TOPIC_MEMBER_CANCEL = TOPIC_MEMBER_CANCEL;
    }

    public String getTAG_MEMBER_CANCEL() {
        return TAG_MEMBER_CANCEL;
    }

    public void setTAG_MEMBER_CANCEL(String TAG_MEMBER_CANCEL) {
        this.TAG_MEMBER_CANCEL = TAG_MEMBER_CANCEL;
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
