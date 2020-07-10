package com.kakarote.crm9.integration.mq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.jfinal.aop.Aop;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.integration.controller.MqMessageCronController;
import com.kakarote.crm9.integration.service.SiteMemberMqMessageService;
import com.kakarote.crm9.utils.BaseUtil;
import com.qxwz.lyra.common.mq.consumer.LyraMqConsumer;

/**
 * 分销商认证成功consumer
 * @author xiaowen.wu
 *
 */
public class ThirdDistributorAuditConsumer implements LyraMqConsumer {

    private Log logger = Log.getLog(MqMessageCronController.class);
    
    private String topic;

    private String auditTag;

    private NotifyService notifyService;
    private String warningEmailTopic;
    private String warningEmailToList;

    private SiteMemberMqMessageService siteMemberMqMessageService = Aop.get(SiteMemberMqMessageService.class);
	
	@Override
	public String topic() {

		return topic;
	}

	@Override
	public List<String> tags() {

		List<String> tags = new ArrayList<String>();
		tags.add(auditTag);
//		tags.add(frozenTag); 不订阅分销商冻结/解冻消息
		return tags;
	}

	@Override
	public void execute(MessageExt msg, Object obj) {

        try {
            if (Objects.nonNull(msg) && Objects.nonNull(obj)) {
                String tag = msg.getTags();
                logger.info(String.format("#############ThirdDistributorAuditConsumer ---> tag is: %s", tag));
                if (auditTag.equals(tag)) {
                    logger.info(String.format("#############Distributor data: msg is: %s, obj is: %s", msg.toString(), obj.toString()));
                    if (obj instanceof String) {
                        obj = JSON.parseObject((String) obj);
                    }
                    // 落地分销商信息，后续定时任务处理
                    siteMemberMqMessageService.saveMqMsg2Crm(msg, obj);
                }
            }
        } catch (Exception e) {
            String ex = BaseUtil.getExceptionStack(e);
            logger.error("#############ThirdDistributorAuditConsumer has exception:" + ex);
            if (Objects.nonNull(warningEmailToList)) {
                List<String> toList = Arrays.asList(warningEmailToList.split(","));
                notifyService.email(warningEmailTopic, ex, toList);
            }
        }
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

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getAuditTag() {
		return auditTag;
	}

	public void setAuditTag(String auditTag) {
		this.auditTag = auditTag;
	}
    
}
