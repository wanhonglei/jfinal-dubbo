package com.kakarote.crm9.integration.mq;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.JsonKit;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesAbnormalFunds;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.utils.Assert;
import com.qxwz.merak.payment.intervention.model.bo.InterventionPaymentMessageModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/17 4:30 下午
 */
@Slf4j(topic = "com.kakarote.crm9.integration.controller.MqMessageCronController")
public class BopsAbnormalFundsConsumer extends BaseMqConsumer {

    private String topic;

    private String tag;

    @Override
    protected boolean consume(MqMsg msg) {
        Assert.notNull(msg, "消息对象为空");
        Assert.notBlank(msg.getContent(), "消息体为空");
        InterventionPaymentMessageModel dto = JSON.parseObject(msg.getContent(), InterventionPaymentMessageModel.class);
        Assert.notNull(dto, "消息转换失败");
        CrmReceivablesAbnormalFunds existFund = CrmReceivablesAbnormalFunds.dao.getByPaymentNo(dto.getPaymentNo());
        if (existFund == null) {
            log.info("异常资金插入[content:{}]", JsonKit.toJson(dto));
            existFund = new CrmReceivablesAbnormalFunds();
            existFund.setPaymentNo(dto.getPaymentNo());
            existFund.setOaNo(dto.getOaNo());
            existFund.setFundsState(BopsFundsState.getCodeByRemoteCode(dto.getInterventionStatus()));
            existFund.save();
        } else {
            log.info("异常资金更新[id:{}][state from:{} to:{}]", existFund.getId(), existFund.getFundsState(), BopsFundsState.getCodeByRemoteCode(dto.getInterventionStatus()));
            existFund.setOaNo(dto.getOaNo());
            existFund.setFundsState(BopsFundsState.getCodeByRemoteCode(dto.getInterventionStatus()));
            existFund.update();
        }
        return true;
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public List<String> tags() {
        return Collections.singletonList(tag);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public enum BopsFundsState {
        Unprocessed("new", 0, "未处理"),
        Processing("applying", 1, "处理中"),
        Done("done", 2, "处理完成"),
        Closed("closed", 3, "关闭"),
        ;

        public static BopsFundsState getByCode(Integer code) {
            for (BopsFundsState value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return null;
        }

        public static BopsFundsState getByRemoteCode(String remoteCode) {
            for (BopsFundsState value : values()) {
                if (value.remoteCode.equals(remoteCode)) {
                    return value;
                }
            }
            return null;
        }

        public static Integer getCodeByRemoteCode(String remoteCode) {
            BopsFundsState bopsFundsState = getByRemoteCode(remoteCode);
            return bopsFundsState == null ? null : bopsFundsState.getCode();
        }

        private String remoteCode;
        private Integer code;
        private String desc;

        BopsFundsState(String remoteCode, Integer code, String desc) {
            this.remoteCode = remoteCode;
            this.code = code;
            this.desc = desc;
        }

        public String getRemoteCode() {
            return remoteCode;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
