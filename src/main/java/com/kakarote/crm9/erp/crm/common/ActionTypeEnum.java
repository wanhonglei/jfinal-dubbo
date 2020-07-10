package com.kakarote.crm9.erp.crm.common;

/**
 * 行为类型
 * @author liming.guo
 */
public enum ActionTypeEnum {

    ADD("add", "新增"),
    UPDATE("update", "修改"),
    DELETE("delete", "删除");

    public static ActionTypeEnum findByType(String type) {
        for (ActionTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    private String type;

    private String desc;

    ActionTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

}
