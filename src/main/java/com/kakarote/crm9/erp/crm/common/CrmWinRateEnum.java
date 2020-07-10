package com.kakarote.crm9.erp.crm.common;

public enum CrmWinRateEnum {
    /**
     * CrmWinRateEnum
     */
    WON_TYPE_KEY("赢单", "Won"),
    WORST_TYPE_KEY("最差的", "Worst"),
    FORECAST_TYPE_KEY("预测", "Forecast"),
    RISK_TYPE_KEY("风险", "Risk"),
    LOST_TYPE_KEY("输单", "Lost"),
    BEST_TYPE_KEY("最好的","Best");

    private String name;
    private String types;
    CrmWinRateEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmWinRateEnum c : CrmWinRateEnum.values()) {
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
        CrmWinRateEnum[] items = CrmWinRateEnum.values();
        for(CrmWinRateEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
