package com.kakarote.crm9.common.midway;

import com.jfinal.log.Log;
import com.qxwz.midway.api.enums.MailTypeEnum;
import com.qxwz.midway.api.model.CorpChatRequest;
import com.qxwz.midway.api.model.MailSendRequest;
import com.qxwz.midway.api.model.MidwayResponse;
import com.qxwz.midway.client.MidwaySender;

import java.util.List;

/**
 * Midway notify service
 *
 * @author hao.fu
 * @create 2019/7/12 14:33
 */
public class NotifyService {

    private Log logger = Log.getLog(getClass());

    private MidwaySender midwaySender;

    private String dingTalkAgentId;

    public void email(String subject, String body, List<String> toEmailList) {
        MailSendRequest request = new MailSendRequest();
        request.setMailType(MailTypeEnum.INTERNAL_NORMAL.getCode());
        request.setSubject(subject);
        request.setHtmlBody(body);
        toEmailList.forEach(request::addToAddress);
        midwaySender.syncSendMail(request);
        logger.info("async send email. request subject: " + subject + ", toEmailList:" + toEmailList);
    }

    public void dingTalk(String content, List<String> toUsers) {
        CorpChatRequest request = new CorpChatRequest();
        // 微应用id
        request.setAgentId(dingTalkAgentId);
        // 消息内容
        request.setContent(content);
        // 用户id列表
        request.setToUserList(toUsers);
        MidwayResponse response = midwaySender.sendCorpChat(request);
        logger.info("send dingTalk. request: " + request + ", response:" + response);
    }

    public MidwaySender getMidwaySender() {
        return midwaySender;
    }

    public void setMidwaySender(MidwaySender midwaySender) {
        this.midwaySender = midwaySender;
    }

    public String getDingTalkAgentId() {
        return dingTalkAgentId;
    }

    public void setDingTalkAgentId(String dingTalkAgentId) {
        this.dingTalkAgentId = dingTalkAgentId;
    }
}
