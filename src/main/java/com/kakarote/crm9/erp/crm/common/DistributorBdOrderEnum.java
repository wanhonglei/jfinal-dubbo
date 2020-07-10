package com.kakarote.crm9.erp.crm.common;

public enum DistributorBdOrderEnum {

    DEPT_NAME("deptName", "大区名称"),
    SALE_AREA_NAME("saleAreaName","省份"),
    BD_NAME("bdName","负责bd"),
    SUM_SALES_QUANTITY("sumSalesQuantity","采购数量"),
    SUM_DELIVERY_QUANTITY("sumDeliveryQuantity","发货数量"),
    BD_SALES_QUANTITY("bdSalesQuantity","bd销售数量"),
    THEORETICAL_INVENTORY("theoreticalInventory","理论库存"),
    ACTUAL_INVENTORY("actualInventory","实际库存");

    private String code;
    private String name;
    DistributorBdOrderEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public static String getCode(String name) {
        for (DistributorBdOrderEnum c : DistributorBdOrderEnum.values()) {
            if (c.getName().equals(name)) {
                return c.code;
            }
        }
        return "";
    }
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getTypeByName(String name) {
        DistributorBdOrderEnum[] items = DistributorBdOrderEnum.values();
        for(DistributorBdOrderEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getCode();
            }
        }
        return "";
    }
}
