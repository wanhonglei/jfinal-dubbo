package com.kakarote.crm9.erp.crm.common.contract;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/15 5:44 下午
 */
public enum CheckStatusEnum {
    PROGRESSING(1, "审核中"),
    PASS(2, "审核通过"),
    REJECT(3, "审核未通过"),
    CANCEL(4, "作废"),
    ;

    public static CheckStatusEnum findByCode(Integer code) {
        for (CheckStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    private Integer code;
    private String desc;

    CheckStatusEnum(Integer code, String desc) {
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
