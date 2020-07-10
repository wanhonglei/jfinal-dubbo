package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: honglei.wan
 * @Description:crm 标签表相关枚举
 * @Date: Create in 2020/2/10 11:00
 */
public enum CrmLabelEnum {
    //关联类型：取 CrmEnum
    /**
     * CrmLabelEnum
     */
    BACK_CUSTOMER_REASON("回归客户池原因", 1);

    private String name;
    private Integer types;
    CrmLabelEnum(String name, Integer types) {
        this.name = name;
        this.types = types;
    }
    public String getName() {
        return name;
    }

    public Integer getTypes() {
        return types;
    }

}
