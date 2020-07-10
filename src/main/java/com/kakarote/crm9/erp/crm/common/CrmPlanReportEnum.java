package com.kakarote.crm9.erp.crm.common;

public enum CrmPlanReportEnum {
    /**
     * CrmPlanReportEnum
     */
    CUSTOMER_NAME_KEY("客户", "customerName"),
    USER_NAME_KEY("BD", "userName"),
    SCENARIO_NAME_KEY("应用场景", "scenarioName"),
    DEPT_NAME_KEY("业务线", "deptName"),
    BUSINESS_NAME("商机名称","businessName"),
    PARTNER_KEY("合作伙伴(合同甲方)", "partner"),
    PROJECT_KEY("客户项目", "project"),
    PRODUCT_NAME("商品", "productName"),
    BUSINESS_STATUS("商机阶段", "busnessStatus"),
    DEAL_DATE("预计成交日期", "dealDate"),
    MONEY("计划回款", "money"),
    RECEIVEBLES_MONEY("实际回款", "receiveblesMoney"),
    MAP_ADDRESS("省份","mapAddress"),
    ASCRIPTION("归属","ascription"),
    ANNUAL_PRODUCTION("年产量","annualProduction"),
    FIXED_TIME("定点时间","fixedTime"),
    OUTPUT_TIME("量产时间","pruductTime"),
    NUM("期数","num"),
    PLAN_RETURN_DATE("计划回款日期","planReturnDate"),
    PLAN_MONEY("计划回款金额","planMoney"),
    RETURN_TIME("实际回款日期","returnTime"),
    REAL_MONEY("实际回款金额","realMoney"),
    WIN_RATE("赢率","winRate"),
    LOSE_REASON("原因","loseReason"),
    REMARK("备注","remark");


    private String name;
    private String types;
    CrmPlanReportEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmPlanReportEnum c : CrmPlanReportEnum.values()) {
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
}
