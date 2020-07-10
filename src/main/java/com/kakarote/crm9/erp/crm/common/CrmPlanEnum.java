package com.kakarote.crm9.erp.crm.common;

public enum CrmPlanEnum {
    /**
     * CRM Enum
     */
    WIN_TYPE_KEY("赢单", "win"),
    LOSE_TYPE_KEY("输单", "lose");

    private String name;
    private String types;
    CrmPlanEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmPlanEnum c : CrmPlanEnum.values()) {
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
        CrmPlanEnum[] items = CrmPlanEnum.values();
        for(CrmPlanEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
