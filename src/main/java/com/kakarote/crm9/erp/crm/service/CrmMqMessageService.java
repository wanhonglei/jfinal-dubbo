package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.integration.controller.MqMessageCronController;

import java.util.Date;
import java.util.List;

/**
 * CRM MQ message service
 *
 * @author hao.fu
 * @since 2019/8/16 13:44
 */
public class CrmMqMessageService {

    private Log logger = Log.getLog(MqMessageCronController.class);

    public Record findMessagByMsgId(String msgId) {
        return Db.findFirst(Db.getSql("crm.mqmessage.findMessagByMsgId"), msgId);
    }

    public List<Record> getUnconsumedMessage() {
        return Db.find(Db.getSql("crm.mqmessage.getUnconsumedMessage"));
    }

    public void updateConsumeStatus(Long id) {
        Db.update(Db.getSql("crm.mqmessage.updateConsumeStatus"), id);
    }

    public MqMsg saveMqMsg2Crm(MessageExt msg, Object obj) {
        String msgId = msg.getMsgId();

        Record msgInDb = findMessagByMsgId(msgId);
        logger.info(String.format("get mq msg from crm db, msgInDb: %s", msgInDb != null ? msgInDb.toJson() : null));
        if (msgInDb == null) {
            MqMsg mqMsg = new MqMsg();
            mqMsg.setMsgId(msgId);
            mqMsg.setTopic(msg.getTopic());
            mqMsg.setTag(msg.getTags());
            mqMsg.setContent(JSON.toJSONString(obj));
            mqMsg.setCreateTime(new Date());
            mqMsg.setUpdateTime(new Date());
            logger.info(String.format("save MQ message to CRM db: %s", mqMsg));
            mqMsg.save();
            return mqMsg;
        } else {
            logger.info(String.format("MQ message exists in CRM db: msg is: %s, obj is: %s", msg.toString(), obj.toString()));
        }
        return null;
    }

    public List<MqMsg> findUnConsumedMessageByTopicAndTags(String topic, List<String> tags) {
        return MqMsg.dao.find(Db.getSqlPara("crm.mqmessage.findUnConsumedMessageByTopicAndTags", Kv.by("topic", topic).set("tags", tags)));
    }
}
