package com.kakarote.crm9.erp.crm.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/3/25 11:59 上午
 */
public enum CustomerStorageTypeEnum {
    /**
     * CustomerStorageTypeEnum
     */
    INSPECT_CAP(1, "考察库"),
    RELATE_CAP(2, "关联库");

    private int code;
    private String name;

    CustomerStorageTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(int code) {
        for (CustomerStorageTypeEnum c : CustomerStorageTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String Name) {
        for (CustomerStorageTypeEnum c : CustomerStorageTypeEnum.values()) {
            if (c.getName().equals(Name)) {
                return c.code;
            }
        }
        return null;
    }

    public static CustomerStorageTypeEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (CustomerStorageTypeEnum c : CustomerStorageTypeEnum.values()) {
            if (Objects.equals(c.getCode(), code)) {
                return c;
            }
        }
        return null;
    }

    public static Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>(values().length);
        for (CustomerStorageTypeEnum value : values()) {
            map.put(String.valueOf(value.getCode()), value.getName());
        }
        return map;
    }
}
