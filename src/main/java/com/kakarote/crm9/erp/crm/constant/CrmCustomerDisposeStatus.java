package com.kakarote.crm9.erp.crm.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/8 2:07 下午
 */
public enum CrmCustomerDisposeStatus {
    TO_BE_FOLLOWED_UP(0, "待跟进"),
    FOLLOWING(1, "跟进中"),
    ;

    public static Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>(values().length);
        for (CrmCustomerDisposeStatus value : values()) {
            map.put(String.valueOf(value.getCode()), value.getDesc());
        }
        return map;
    }

    public static CrmCustomerDisposeStatus findByCode(Integer code) {
        for (CrmCustomerDisposeStatus value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    private Integer code;
    private String desc;

    CrmCustomerDisposeStatus(java.lang.Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public java.lang.Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
