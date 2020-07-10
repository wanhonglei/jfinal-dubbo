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
 * Site Member Auth Success Consumer
 *
 * @author hao.fu
 * @since 2019/7/1 18:09
 */
public class SiteMemberAuthSuccessConsumer implements LyraMqConsumer {

    private Log logger = Log.getLog(getClass());

    private String TOPIC_MEMBER_AUDIT_SUCCESS;
    private String TAG_PERSONAL;
    private String TAG_COMPANY;
    private NotifyService notifyService;
    private String warningEmailTopic;
    private String warningEmailToList;

    private CrmMqMessageService mqMessageService = Aop.get(CrmMqMessageService.class);

    @Override
    public String topic() {
        return TOPIC_MEMBER_AUDIT_SUCCESS;
    }

    @Override
    public List<String> tags() {
        List<String> tags = Lists.newArrayList();
        tags.add(TAG_PERSONAL);
        tags.add(TAG_COMPANY);
        return tags;
    }

    @Override
    public void execute(MessageExt msg, Object obj) {
        try {
            if (Objects.nonNull(msg) && Objects.nonNull(obj)) {
                String tag = msg.getTags();
                logger.info(String.format("#############SiteMemberAuthSuccessConsumer ---> tag is: %s", tag));

                if (TAG_PERSONAL.equals(tag) || TAG_COMPANY.equals(tag)) {

                    logger.info(String.format("#############From MQ, msg is: %s, obj is: %s", msg.toString(), obj.toString()));
                    if (obj instanceof String) {
                        obj = JSON.parseObject(obj.toString());
                    }
                    mqMessageService.saveMqMsg2Crm(msg, obj);
                }
            }
        } catch (Exception e) {
            String ex = BaseUtil.getExceptionStack(e);
            logger.error("#############SiteMemberAuthSuccessConsumer has exception:" + ex);
            if (warningEmailToList != null && warningEmailToList.length() > 0) {
                List<String> toList = Arrays.asList(warningEmailToList.split(","));
                notifyService.email(warningEmailTopic, ex, toList);
            }
        }
    }


    public String getTOPIC_MEMBER_AUDIT_SUCCESS() {
        return TOPIC_MEMBER_AUDIT_SUCCESS;
    }

    public void setTOPIC_MEMBER_AUDIT_SUCCESS(String TOPIC_MEMBER_AUDIT_SUCCESS) {
        this.TOPIC_MEMBER_AUDIT_SUCCESS = TOPIC_MEMBER_AUDIT_SUCCESS;
    }

    public String getTAG_PERSONAL() {
        return TAG_PERSONAL;
    }

    public void setTAG_PERSONAL(String TAG_PERSONAL) {
        this.TAG_PERSONAL = TAG_PERSONAL;
    }

    public String getTAG_COMPANY() {
        return TAG_COMPANY;
    }

    public void setTAG_COMPANY(String TAG_COMPANY) {
        this.TAG_COMPANY = TAG_COMPANY;
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
