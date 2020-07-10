package com.kakarote.crm9.erp.crm.common;

public enum CrmProductReceivablesReportEnum {
    /**
     * CrmProductReceivablesReportEnum
     */
    DEPT_NAME_KEY("部门", "deptName"),
    SCENARIO_NAME_KEY("场景", "scenarioName"),
    CATEGORY_NAME_KEY("商品类别", "categoryName"),
    PRODUCT_NAME_KEY("商品名称","productName"),
    PRODUCT_NUM_KEY("商品数量","productNum"),
    NUM_KEY("期数","num"),
    RECEIVABLES_PLAN_TIME_KEY("计划回款日期", "receivablesPlanTime"),
    RECEIVABLES_PLAN_NAME_KEY("计划回款金额", "receivablesPlanMoney"),
    RECEIVABLES_TIME_KEY("实际回款日期", "receivablesTime"),
    RECEIVABLES_MONEY_KEY("实际回款金额", "receivablesMoney"),
    WIN_RATE_KEY("赢率", "winRate"),
    RECEIVABLES_REMARK_KEY("备注","receivablesRemark");

    private String name;
    private String types;
    CrmProductReceivablesReportEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmProductReceivablesReportEnum c : CrmProductReceivablesReportEnum.values()) {
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
