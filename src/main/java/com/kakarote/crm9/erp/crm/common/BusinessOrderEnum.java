package com.kakarote.crm9.erp.crm.common;

import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/7 5:32 下午
 */
public enum BusinessOrderEnum {
    // 升序
    ASC("2", "升序"),
    // 降序
    DESC("1", "降序"),
    ;

    public static String getByCode(String code) {
        for (BusinessOrderEnum value : values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value.name();
            }
        }
        return null;
    }

    private String code;
    private String desc;

    BusinessOrderEnum(String code,  String desc) {
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
