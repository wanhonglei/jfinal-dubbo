package com.kakarote.crm9.erp.crm.common;

public enum CrmSaleUsualDetailExcelEnum {
    /**
     * CrmSaleUsualExcelEnum
     */
    SALES_USER_KEY("BD", "bd"),
    PRODUCT_CATEGORY_KEY("商品大类","productCategory"),
    PRODUCT_KEY("商品", "productName"),
    INCOME_KEY("收入","revenue");

    private String name;
    private String types;
    CrmSaleUsualDetailExcelEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmSaleUsualDetailExcelEnum c : CrmSaleUsualDetailExcelEnum.values()) {
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
        CrmSaleUsualDetailExcelEnum[] items = CrmSaleUsualDetailExcelEnum.values();
        for(CrmSaleUsualDetailExcelEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
