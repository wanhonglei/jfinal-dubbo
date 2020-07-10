package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/26 4:51 下午
 */
public enum DistributorFreezeStatusEnum {
    FROZEN(1, "冻结"),
    UNFROZEN(2, "解冻"),
    ;

    private Integer code;
    private String desc;

    DistributorFreezeStatusEnum(Integer code, String desc) {
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
