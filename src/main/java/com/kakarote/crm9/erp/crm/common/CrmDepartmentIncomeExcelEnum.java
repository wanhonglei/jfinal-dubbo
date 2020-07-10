package com.kakarote.crm9.erp.crm.common;

public enum CrmDepartmentIncomeExcelEnum {
    /**
     * CrmDepartmentIncomeExcelEnum
     */
    PRODUCT_MAIN_INCOME_DESC_KEY("当期收入汇总(单位：元)", "productMainDesc"),
    PRODUCT_DETAIL_INCOME_DESC_KEY("当期收入明细(单位：元，以省为纬度)","productDetailDesc");

    private String name;
    private String types;
    CrmDepartmentIncomeExcelEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmDepartmentIncomeExcelEnum c : CrmDepartmentIncomeExcelEnum.values()) {
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
        CrmDepartmentIncomeExcelEnum[] items = CrmDepartmentIncomeExcelEnum.values();
        for(CrmDepartmentIncomeExcelEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
