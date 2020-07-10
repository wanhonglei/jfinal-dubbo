package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/12 2:44 下午
 */
public enum PerformanceTargetTypeEnum {
    /**
     * 客户
     */
    CUSTOMER(1, "客户"),
    ;


    private Integer code;
    private String desc;

    PerformanceTargetTypeEnum(Integer code, String desc) {
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
