package com.kakarote.crm9.erp.crm.common;

import java.util.Objects;

/**
 * 分销商用户类型
 *
 * @author liming.guo
 */
public enum SiteMemberUserTypeEnum {

    /**
     * 0 个人用户
     */
    PERSONAL(0, "个人用户"),
    /**
     * 1 企业用户
     */
    COMPANY(1, "企业用户");

    private Integer code;

    private String desc;

    SiteMemberUserTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过code获取枚举值
     *
     * @param code
     * @return
     */
    public static SiteMemberUserTypeEnum getUserTypeByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (SiteMemberUserTypeEnum userTypeEnum : values()) {
            if (Objects.equals(userTypeEnum.getCode(), code)) {
                return userTypeEnum;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
