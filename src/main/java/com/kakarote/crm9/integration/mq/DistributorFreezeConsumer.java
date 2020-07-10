package com.kakarote.crm9.integration.mq;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.erp.crm.common.DistributorFreezeStatusEnum;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.CrmSiteMemberService;
import com.kakarote.crm9.integration.entity.DistributorAuditDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/26 1:43 下午
 */
@Slf4j(topic = "com.kakarote.crm9.integration.controller.MqMessageCronController")
public class DistributorFreezeConsumer extends BaseDistributorConsumer {

    private String topic;

    private String tag;

    private CrmSiteMemberService crmSiteMemberService = Aop.get(CrmSiteMemberService.class);

    @Override
    protected boolean consume(MqMsg msg) {
        if (!Objects.equals(tag, msg.getTag())) {
            return false;
        }
        DistributorAuditDTO dto = JSON.toJavaObject(JSON.parseObject(msg.getContent()), DistributorAuditDTO.class);
        if (Objects.isNull(dto)) {
            log.error("DistributorFreezeConsumer consume msg[id:{},msgId:{}] fail , content is null", msg.getId(), msg.getMsgId());
            return false;
        }
        Integer frozenStatus = getFrozenStatus(dto.getDistributorStatus());
        if (Objects.isNull(frozenStatus)) {
            log.error("DistributorFreezeConsumer consume msg[id:{},msgId:{}] fail , unknown frozen status[{}]", msg.getId(), msg.getMsgId(), dto.getDistributorStatus());
            return false;
        }
        /* 更新网站会员冻结解冻状态 */
        crmSiteMemberService.updateFreezeStatusBySiteMemberId(dto.getId(), frozenStatus);
        return true;
    }

    private Integer getFrozenStatus(Integer code) {
        DistributorStatus distributorStatus = DistributorStatus.getByCode(code);
        if (Objects.nonNull(distributorStatus)) {
            if (distributorStatus == DistributorStatus.FREEZE) {
                return DistributorFreezeStatusEnum.FROZEN.getCode();
            }
            return DistributorFreezeStatusEnum.UNFROZEN.getCode();
        }
        return null;
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

    /**
     * 分销商状态
     */
    private enum DistributorStatus {
        FAIL(1, "审核不通过"),
        AUDITING(2, "待审核"),
        SUCCESS(3, "已认证"),
        FREEZE(4, "已冻结"),
        REAPPLY(5, "待完善"),
        ;

        private Integer code;
        private String desc;

        public static DistributorStatus getByCode(Integer code) {
            for (DistributorStatus value : values()) {
                if (Objects.equals(value.getCode(), code)) {
                    return value;
                }
            }
            return null;
        }

        DistributorStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
