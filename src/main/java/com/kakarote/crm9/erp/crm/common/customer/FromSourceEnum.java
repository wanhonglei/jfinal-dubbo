package com.kakarote.crm9.erp.crm.common.customer;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/12 5:06 下午
 */
public enum FromSourceEnum {
    BY_MANUAL(1, "手工创建"),
    FROM_WEBSITE(2, "官网同步"),
    FROM_LEADS(3, "线索转化"),
    ;


    private Integer code;
    private String desc;

    FromSourceEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>(values().length);
        for (FromSourceEnum value : values()) {
            map.put(String.valueOf(value.getCode()), value.getDesc());
        }
        return map;
    }

    public static String getDescByCode(Integer code) {
        for (FromSourceEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getDesc();
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
