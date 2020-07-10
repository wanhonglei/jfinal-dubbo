package com.kakarote.crm9.erp.admin.common;

/**
 * 商品大类枚举类
 * @author yue.li
 * @date 2019/11/19
 */
public enum ProductCategoryEnum {
    /**
     * ProductCategoryEnum
     */
    CATEGORY_ENTITY_KEY("实物", "category$entity"),
    CATEGORY_VIRTUAL_KEY("服务", "category$virtual"),
    CUSTOMER_PACKAGE_KEY("套装", "category$package"),
    CUSTOMER_NOTHING_KEY("定制", "category$nothing");

    private String name;
    private String types;
    ProductCategoryEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (ProductCategoryEnum productCategoryEnum : ProductCategoryEnum.values()) {
            if (productCategoryEnum.getTypes().equals(types)) {
                return productCategoryEnum.name;
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

    public static String getTypeByName(String name) {
        ProductCategoryEnum[] items = ProductCategoryEnum.values();
        for(ProductCategoryEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }

    /**
     * 根据需要找到对应枚举
     * @param ordinal
     * @return
     */
    public static ProductCategoryEnum getEnumByOrdinal(int ordinal) {
        for (ProductCategoryEnum productCategoryEnum : ProductCategoryEnum.values()) {
            if (productCategoryEnum.ordinal() == ordinal) {
                return productCategoryEnum;
            }
        }
        return null;
    }
}
