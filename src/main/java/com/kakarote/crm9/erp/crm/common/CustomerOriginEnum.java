package com.kakarote.crm9.erp.crm.common;
/**
 * CustomerOriginEnum class
 *
 * @author yue.li
 * @date 2019/11/22
 */
public enum CustomerOriginEnum {
    /**
     * CustomerOriginEnum
     */
    CUSTOMER_NEW_KEY("客户新建", "0"),
    LEADS_TRANSFORM_KEY("线索转化", "1"),
    WEB_SITE_ORIGIN_KEY("官网注册", "3");

    private String name;
    private String types;
    CustomerOriginEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CustomerOriginEnum customerOriginEnum : CustomerOriginEnum.values()) {
            if (customerOriginEnum.getTypes().equals(types)) {
                return customerOriginEnum.name;
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
        CustomerOriginEnum[] items = CustomerOriginEnum.values();
        for(CustomerOriginEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
