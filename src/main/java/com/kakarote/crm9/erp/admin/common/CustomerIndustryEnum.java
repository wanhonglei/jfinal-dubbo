package com.kakarote.crm9.erp.admin.common;

public enum CustomerIndustryEnum {
    EXCLUSIVE(1, "专属客户行业"),
    SHARE(2, "共享客户行业"),
    ;

    private Integer code;
    private String desc;

    CustomerIndustryEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}