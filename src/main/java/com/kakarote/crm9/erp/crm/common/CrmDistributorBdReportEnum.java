package com.kakarote.crm9.erp.crm.common;

public enum CrmDistributorBdReportEnum {

    /**
     * CrmDistributorReportEnum
     */
    DEPT_NAME("大区", "deptName"),
    CUSTOMER_NAME("分销商", "customerName"),
    SALE_AREA_NAME("销售区域（省市）", "saleAreaName"),
    PRODUCT_NAME("商品", "productName"),
    BD_NAME("负责BD", "bdName"),
    GOODS_SPEC("商品规格", "goodsSpec"),
    SUM_SALES_QUANTITY("采购数量", "sumSalesQuantity"),
    SUM_DELIVERY_QUANTITY("已发货数量","sumDeliveryQuantity"),
    BD_SALES_QUANTITY("销售数量", "bdSalesQuantity"),
    THEORETICAL_INVENTORY("理论库存", "theoreticalInventory"),
    ACTUAL_INVENTORY("真实库存", "actualInventory");

    private String name;
    private String types;
    CrmDistributorBdReportEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmDistributorBdReportEnum c : CrmDistributorBdReportEnum.values()) {
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
