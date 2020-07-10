package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/16 11:28 上午
 */
public enum PerformanceObjectTypeEnum {
    DEPARTMENT(1, "部门"),
    BD(2, "BD"),
    ;

    private Integer code;
    private String desc;

    PerformanceObjectTypeEnum(Integer code, String desc) {
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
