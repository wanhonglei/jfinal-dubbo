package com.kakarote.crm9.integration.mq;

import com.kakarote.crm9.erp.crm.common.DistributorCertifiedEnum;
import com.kakarote.crm9.erp.crm.common.DistributorPartnerTypeEnum;
import com.kakarote.crm9.erp.crm.common.PromotionTagEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/26 10:50 上午
 */
@Slf4j(topic = "com.kakarote.crm9.integration.controller.MqMessageCronController")
public abstract class BaseDistributorConsumer extends BaseMqConsumer {

    /**
     * 获取生态伙伴类型
     *
     * @param ambassadorType
     * @return
     */
    protected DistributorPartnerTypeEnum getAmbassadorType(String ambassadorType) {
        return DistributorPartnerTypeEnum.getByCode(ambassadorType);
    }

    /**
     * 获取生态伙伴描述
     *
     * @param ambassadorType
     * @return
     */
    protected String getAmbassadorTypeDesc(String ambassadorType) {
        DistributorPartnerTypeEnum type = getAmbassadorType(ambassadorType);
        return Objects.isNull(type) ? ambassadorType : type.getDesc();
    }

    /**
     * 获取推广标签
     *
     * @param code
     * @return
     */
    protected PromotionTagEnum getPromotionTag(String code) {
        RemotePromotionTagEnum promotionTagEnum = RemotePromotionTagEnum.getByCode(code);
        if (Objects.isNull(promotionTagEnum)) {
            return null;
        }
        switch (promotionTagEnum) {
            case EU:
                return PromotionTagEnum.TerminalUser;
            case LEVEL:
                return PromotionTagEnum.Distributor;
            default:
        }
        return null;
    }

    /**
     * 获取推广标签
     *
     * @param code
     * @param level 分销商等级 非分销商不处理
     * @return
     */
    protected String getPromotionTagCode(String code, Integer level) {
        PromotionTagEnum promotionTag = getPromotionTag(code);
        if (Objects.nonNull(promotionTag)) {
            switch (promotionTag) {
                case TerminalUser:
                    return promotionTag.getCode();
                case Distributor:
                    return transDistributorCode(level);
                default:
            }
        }
        return null;
    }

    private String transDistributorCode(Integer level) {
        if (Objects.isNull(level)) {
            return "";
        }
        switch (level) {
            case 1:
                return PromotionTagEnum.DistributorL1.getCode();
            case 2:
                return PromotionTagEnum.DistributorL2.getCode();
            case 3:
                return PromotionTagEnum.DistributorL3.getCode();
            case 4:
                return PromotionTagEnum.DistributorL4.getCode();
            default:
        }
        return "";
    }

    /**
     * 推广标签
     */
    protected enum RemotePromotionTagEnum {
        LEVEL("level", "层级"),
        EU("eu", "终端"),
        ;

        private String code;
        private String desc;

        public static RemotePromotionTagEnum getByCode(String code) {
            for (RemotePromotionTagEnum value : values()) {
                if (Objects.equals(value.getCode(), code)) {
                    return value;
                }
            }
            return null;
        }

        RemotePromotionTagEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 分销商认证状态
     */
    protected enum RemoteDistributorAuditStatus {
        A(1, "仅注册"),
        B(2, "审核中"),
        C(3, "审核通过"),
        D(4, "审核未通过"),
        E(5, "手机和邮箱信息已补齐，但未认证"),
        ;

        public static Integer getCertifiedByCode(Integer code) {
            RemoteDistributorAuditStatus distributorAuditStatus = findByCode(code);
            if (distributorAuditStatus == null) {
                return null;
            }

            switch (distributorAuditStatus) {
                case A:
                case B:
                case D:
                case E:
                    return DistributorCertifiedEnum.UN_AUDIT.getCode();
                case C:
                    return DistributorCertifiedEnum.AUDIT.getCode();
                default:
            }
            return DistributorCertifiedEnum.UN_AUDIT.getCode();
        }

        private static RemoteDistributorAuditStatus findByCode(Integer code) {
            for (RemoteDistributorAuditStatus value : values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }

        private Integer code;
        private String desc;

        RemoteDistributorAuditStatus(Integer code, String desc) {
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
