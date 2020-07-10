package com.kakarote.crm9.erp.crm.common;

import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/26 8:52 下午
 */
public enum DistributorPartnerTypeEnum {
    distributor("dr_offline", "千寻位置分销伙伴"),
    ambassador("ambassador", "千寻位置大使"),
    ;

    private String code;
    private String desc;

    public static DistributorPartnerTypeEnum getByCode(String code) {
        for (DistributorPartnerTypeEnum value : values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value;
            }
        }
        return null;
    }

    DistributorPartnerTypeEnum(String code, String desc) {
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
