package com.kakarote.crm9.erp.crm.common;

public enum CrmSaleUsualExcelEnum {
    /**
     * CrmSaleUsualExcelEnum
     */
    SALE_USER_NAME_KEY("BD","saleUserName"),
    VISIT_COUNT_KEY("拜访数量", "visitCount"),
    RELATION_COUNT_KEY("联系数量", "relationCount"),
    TOTAL_VISIT_RELATION_COUNT_KEY("总拜访联系数量","totalVisitRelationCount"),
    REGION_CUSTOMER_COUNT_KEY("地市客户拜访联系数","regionCustomerCount"),
    REGION_CUSTOMER_RATIO("地市客户占比率","regionCustomerRatio"),
    TOTAL_WIN_BILLS_COUNT("总成单数","totalWinBillsCount"),
    VISIT_WIN_BILLS_COUNT("拜访成单数","visitWinBillsCount"),
    TOTAL_WIN_BILLS_RATIO("总成单比","totalWinBillsRatio"),
    VISIT_WIN_BILLS_RATIO("拜访成单比","visitWinBillsRatio"),
    RELEVANCE_TOTAL_INCOME("收入(已关联回款计划)","relevanceTotalIncome"),
    NO_RELEVANCE_TOTAL_INCOME("收入(未关联回款计划)","noRelevanceTotalIncome"),
    TOTAL_INCOME("总收入","totalIncome");

    private String name;
    private String types;
    CrmSaleUsualExcelEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmSaleUsualExcelEnum c : CrmSaleUsualExcelEnum.values()) {
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
        CrmSaleUsualExcelEnum[] items = CrmSaleUsualExcelEnum.values();
        for(CrmSaleUsualExcelEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
