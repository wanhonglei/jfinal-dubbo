package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/26 11:57 上午
 */
public enum DistributorCertifiedEnum {
    UN_AUDIT(1, "未认证"),
    AUDIT(2, "已认证");


    private Integer code;
    private String desc;

    DistributorCertifiedEnum(Integer code, String desc) {
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
