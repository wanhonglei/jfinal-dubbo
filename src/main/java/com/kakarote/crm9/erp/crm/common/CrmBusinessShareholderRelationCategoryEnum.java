package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/23 2:42 下午
 */
public enum CrmBusinessShareholderRelationCategoryEnum {
    ZB("zhongbin", "中兵"),
    ALI("ali", "阿里"),
    OTHER("other", "其他"),
    ;

    private String categoryCode;

    private String categoryName;

    CrmBusinessShareholderRelationCategoryEnum(String categoryCode, String categoryName) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
