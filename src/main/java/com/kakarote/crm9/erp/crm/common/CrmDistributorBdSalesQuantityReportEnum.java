package com.kakarote.crm9.erp.crm.common;

public enum CrmDistributorBdSalesQuantityReportEnum {

    /**
     * CrmDistributorBdSalesQuantityReportEnum
     */
    GMT_CREATE("时间", "gmtCreate"),
    CUSTOMER_NAME("分销商", "customerName"),
    SALE_AREA_NAME("省份", "saleAreaName"),
    BD_NAME("负责BD", "bdName"),
    PRODUCT_NAME("商品", "productName"),
    GOODS_SPEC("商品规格", "goodsSpec"),
    OPERATOR_TYPE("类型", "operationTypeName"),
    BD_SALES_QUANTITY("销售数量", "bdSalesQuantity");

    private String name;
    private String types;
    CrmDistributorBdSalesQuantityReportEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmDistributorBdSalesQuantityReportEnum c : CrmDistributorBdSalesQuantityReportEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return null;
    }
    public String getName() {
        return name;
    }

    public String getTypes() {
        return types;
    }
}
