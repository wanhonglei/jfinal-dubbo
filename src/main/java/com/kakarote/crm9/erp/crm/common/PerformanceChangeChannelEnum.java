package com.kakarote.crm9.erp.crm.common;

/**
 * 绩效改变渠道
 * @Author: haihong.wu
 * @Date: 2020/3/11 2:00 下午
 */
public enum PerformanceChangeChannelEnum {
    CUSTOMER_RECEIVE("customer_receive", "客户领取"),
    CUSTOMER_RECEIVED("customer_received", "客户被领取"),
    ;


    private String code;
    private String desc;

    PerformanceChangeChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
