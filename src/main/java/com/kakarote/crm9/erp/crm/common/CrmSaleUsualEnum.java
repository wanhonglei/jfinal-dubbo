package com.kakarote.crm9.erp.crm.common;

public enum CrmSaleUsualEnum {
    /**
     * CRM Enum
     */
    VISIT_TYPE_KEY("拜访", "0"),
    RELATION_TYPE_KEY("联系", "1"),
    IS_ADDRESS_TYPE_KEY("按照省市区地址查询","2"),
    VISIT_COUNT_TYPE_KEY("拜访数量", "3"),
    RELATION_COUNT_TYPE_KEY("联系数量", "4");
    private String name;
    private String types;
    CrmSaleUsualEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmSaleUsualEnum c : CrmSaleUsualEnum.values()) {
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
        CrmSaleUsualEnum[] items = CrmSaleUsualEnum.values();
        for(CrmSaleUsualEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
