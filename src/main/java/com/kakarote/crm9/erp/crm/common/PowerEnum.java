package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/14 3:54 下午
 */
public enum PowerEnum {
    RW(1, "读写"),
    RO(2, "只读"),
    ;

    public static PowerEnum getByCode(Integer code) {
        for (PowerEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    private Integer code;
    private String desc;

    PowerEnum(Integer code, String desc) {
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
