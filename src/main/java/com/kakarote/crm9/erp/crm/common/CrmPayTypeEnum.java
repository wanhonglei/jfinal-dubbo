package com.kakarote.crm9.erp.crm.common;

public enum CrmPayTypeEnum {
    /**
     * CrmPayTypeEnum
     */
    CONSUME_KEY("消费", "1"),
    RECHARGE_KEY("充值", "2"),
    REFUND_KEY("退款","3");

    private String name;
    private String types;
    CrmPayTypeEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmPayTypeEnum c : CrmPayTypeEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return "";
    }
    public String getName() {
        return name;
    }

    public String getTypes() {
        return types;
    }

    public static String getTypeByName(String name) {
        CrmPayTypeEnum[] items = CrmPayTypeEnum.values();
        for(CrmPayTypeEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return null;
    }
}
