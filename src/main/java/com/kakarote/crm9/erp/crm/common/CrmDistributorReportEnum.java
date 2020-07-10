package com.kakarote.crm9.erp.crm.common;

public enum CrmDistributorReportEnum {

    /**
     * CrmDistributorReportEnum
     */
    DEPT_NAME("大区", "deptName"),
    SALE_AREA_NAME("省份", "saleAreaName"),
    PRODUCT_NAME("商品", "productName"),
    GOODS_SPEC("商品规格", "goodsSpec"),
    SUM_SALES_QUANTITY("采购数量", "sumSalesQuantity"),
    SUM_DELIVERY_QUANTITY("已发货数量","sumDeliveryQuantity"),
    BD_SALES_QUANTITY("销售数量", "bdSalesQuantity"),
    THEORETICAL_INVENTORY("理论库存", "theoreticalInventory"),
    ACTUAL_INVENTORY("真实库存", "actualInventory");

    private String name;
    private String types;
    CrmDistributorReportEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmDistributorReportEnum c : CrmDistributorReportEnum.values()) {
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
