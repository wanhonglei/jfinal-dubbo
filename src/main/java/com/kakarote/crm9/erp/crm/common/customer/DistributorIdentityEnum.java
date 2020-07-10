package com.kakarote.crm9.erp.crm.common.customer;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 分销身份
 *
 * @Author: haihong.wu
 * @Date: 2020/5/13 7:18 下午
 */
public enum DistributorIdentityEnum {
    /**
     * 分销商没有promotionTag
     */
    DISTRIBUTOR("distributor", "分销商", "none"),
    LEVEL_TWO_DISTRIBUTOR_PARTNER("distributorPartnerL2", "二级分销伙伴", "distributorPartnerL2"),
    DISTRIBUTOR_PARTNER_TERMINAL_USER("terminalUser", "分销伙伴终端用户", "terminalUser"),
    ;

    public static DistributorIdentityEnum getByPromotionTag(String promotionTag) {
        for (DistributorIdentityEnum value : values()) {
            if (Objects.equals(value.getPromotionTag(), promotionTag)) {
                return value;
            }
        }
        return null;
    }

    public static DistributorIdentityEnum getByCode(String code) {
        for (DistributorIdentityEnum value : values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value;
            }
        }
        return null;
    }

    public static Map<String, String> toMap() {
        return Arrays.stream(values()).collect(Collectors.toMap(DistributorIdentityEnum::getCode, DistributorIdentityEnum::getDesc));
    }

    private String code;
    private String desc;
    private String promotionTag;

    DistributorIdentityEnum(String code, String desc, String promotionTag) {
        this.code = code;
        this.desc = desc;
        this.promotionTag = promotionTag;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getPromotionTag() {
        return promotionTag;
    }
}
