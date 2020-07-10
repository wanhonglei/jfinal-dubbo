package com.kakarote.crm9.erp.crm.common;

public enum DistributorBdSalesQuantityOrderEnum {

    GMT_CREATE("gmtCreate", "记录时间"),
    SALE_AREA_NAME("saleAreaName","省份");

    private String code;
    private String name;
    DistributorBdSalesQuantityOrderEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public static String getCode(String name) {
        for (DistributorBdSalesQuantityOrderEnum c : DistributorBdSalesQuantityOrderEnum.values()) {
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
        DistributorBdSalesQuantityOrderEnum[] items = DistributorBdSalesQuantityOrderEnum.values();
        for(DistributorBdSalesQuantityOrderEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getCode();
            }
        }
        return "";
    }
}
