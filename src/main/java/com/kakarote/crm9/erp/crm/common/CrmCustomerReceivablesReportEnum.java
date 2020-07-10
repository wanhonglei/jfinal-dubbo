package com.kakarote.crm9.erp.crm.common;

public enum CrmCustomerReceivablesReportEnum {
    /**
     * CrmCustomerReceivablesReportEnum
     */
    OWNER_USER_NAME_KEY("BD", "ownerUserName"),
    DEPT_NAME_KEY("部门", "deptName"),
    CUSTOMER_NAME_KEY("客户名称", "customerName"),
    ADDRESS_NAME_KEY("省市区", "address"),
    BUSINESS_NAME_KEY("商机名称","businessName"),
    NUM_KEY("期数", "num"),
    RECEIVABLES_PLAN_TIME_KEY("计划回款日期", "receivablesPlanTime"),
    RECEIVABLES_PLAN_NAME_KEY("计划回款金额", "receivablesPlanMoney"),
    RECEIVABLES_TIME_KEY("实际回款日期", "receivablesTime"),
    RECEIVABLES_MONEY_KEY("实际回款金额", "receivablesMoney"),
    WIN_RATE_KEY("赢率", "winRate"),
    LOSE_REASON_KEY("失败原因", "loseReason"),
    RECEIVABLES_REMARK_KEY("回款备注","receivablesRemark");

    private String name;
    private String types;
    CrmCustomerReceivablesReportEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmCustomerReceivablesReportEnum c : CrmCustomerReceivablesReportEnum.values()) {
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
