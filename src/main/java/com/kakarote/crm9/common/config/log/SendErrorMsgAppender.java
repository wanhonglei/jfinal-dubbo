package com.kakarote.crm9.common.config.log;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;

/**
 * @description: 错误日志发送钉钉
 * @author: WanHongLei
 **/
public class SendErrorMsgAppender extends AppenderBase<ILoggingEvent> {

    private PatternLayoutEncoder encoder;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        byte[] payload = this.encoder.encode(iLoggingEvent);
        DingDingMsgSendUtils.sendDingDingGroupMsg(DingTokenEnum.SEND_SMS_BY_DEVELOPER_TOKEN.getToken(),new String(payload), DingSecretEnum.SEND_SMS_BY_DEVELOPER_SECRET.getSecret());
    }


    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

}



@Getter
enum DingMsgPhoneEnum {
    /**
     * @Author: WanHongLei
     * @Description:钉钉消息接收用户，配置钉钉绑定的电话即可
     */

    DEVELOPER_PHONE_all("", "all"),
    DEVELOPER_NONE("", "none"),
    DATA_ANALYST_PHONE("", "数据分析人员");

    private String phone;
    private String name;
    DingMsgPhoneEnum(String phone, String name) {
        this.phone = phone;
        this.name = name;
    }

}


@Getter
enum DingTokenEnum {
    /**
     * @Author: WanHongLei
     * @Description:钉钉消息群机器人access_token
     */

    SEND_SMS_BY_DEVELOPER_TOKEN("447194a5bfe0319f4d9e98dc4c71f90caa702e1444a2a84a9870e9a02439a52a", "系统错误消息通知，技术专用");
    private String token;
    private String name;

    DingTokenEnum(String token, String name) {
        this.token = token;
        this.name = name;
    }

}

@Getter
enum DingSecretEnum {
    /**
     * @Author: WanHongLei
     * @Description:钉钉消息群机器人secret
     */
    SEND_SMS_BY_DEVELOPER_SECRET("SEC893a4861ecd295179c0aced8f280fcdc33f194ac664705e13f328e48725c7a84", "CRM生产报错");
    private String secret;
    private String name;

    DingSecretEnum(String secret, String name) {
        this.secret = secret;
        this.name = name;
    }
}
