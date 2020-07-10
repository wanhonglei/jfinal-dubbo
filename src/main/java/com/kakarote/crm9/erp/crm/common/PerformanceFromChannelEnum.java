package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/1 1:59 下午
 */
public enum PerformanceFromChannelEnum {
    WEBSITE_POOL("WZC", "网站池"),
    MOBILE_SAIL_POOL("DXC", "电销池"),
    DISTRIBUTOR_BIND("FXS", "分销商绑定"),
    ;


    private String code;
    private String desc;

    PerformanceFromChannelEnum(String code, String desc) {
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
