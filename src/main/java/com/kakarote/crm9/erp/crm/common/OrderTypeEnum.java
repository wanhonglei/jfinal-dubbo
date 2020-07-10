package com.kakarote.crm9.erp.crm.common;

public enum OrderTypeEnum {

    DESC("desc", "倒序"),
    ASC("asc","正序");

    private String code;
    private String name;
    OrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public static String getCode(String name) {
        for (OrderTypeEnum c : OrderTypeEnum.values()) {
            if (c.getName().equals(name)) {
                return c.code;
            }
        }
        return "";
    }
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getTypeByName(String name) {
        OrderTypeEnum[] items = OrderTypeEnum.values();
        for(OrderTypeEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getCode();
            }
        }
        return "";
    }
}
