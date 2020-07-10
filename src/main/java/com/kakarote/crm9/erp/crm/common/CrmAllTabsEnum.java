package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: honglei.wan
 * @Description: crm 所有tab页枚举
 * @Date: Create in 2020/4/2 2:01 下午
 */
public enum CrmAllTabsEnum {

    /**
     * crm 所有tab页枚举
     */
    FOLLOWLOG("联系小记","followlog"),
    BASICINFO("基本信息","basicinfo"),
    DISTRIBUTION_INFORMATION("分销信息","distribution-information"),
    CONTACTS("联系人","contacts"),
    CALL_RECORDS("通话记录","call-records"),
    BUSINESS("商机","business"),
    MEMBER_ACCOUNT("网站会员账号","member-account"),
    WEBSITE_ORDER("交易订单","website-order"),
    SERVER_INSTANCE("服务实例","server-instance"),
    DSKACCOUNT("DSK账号","dskaccount"),
    REDEMPTION_CODE("兑换码","redemption-code"),
    VOUCHER("代金券","voucher"),
    INVOICE("发票","invoice"),
    PAYMENT_CHANNEL("支付渠道","payment-channel"),
    ORGANIZATION("组织架构","organization"),
    RELATIVE_HANDLE("操作日志","relative-handle"),
    ;

    private String code;
    private String name;
    CrmAllTabsEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getCode() { return code;}

    public String getName() { return name; }
}
