package com.kakarote.crm9.integration.common;

/**
 * Payment Type Enum
 *
 * @author hao.fu
 * @since 2019/10/11 14:37
 */
public enum PaymentTypeEnum {

    /**
     * 支付宝收款
     */
    ALIPAY_PAY(1, "支付宝收款"),

    /**
     * 银行收款
     */
    BANK_PAY(2, "银行收款"),

    /**
     * 银行提现
     */
    BANK_WITHDRAWAL(3, "银行提现"),

    /**
     * 支付宝退款
     */
    ALIPAY_REFUND(4, "支付宝退款"),

    /**
     * 银行退款
     */
    BANK_REFUND(5, "银行退款"),

    /**
     * 微信支付
     */
    WEIXIN_PAY(6, "微信支付"),

    /**
     * 微信退款
     */
    WEIXIN_REFUND(7, "微信退款"),

    /**
     * 畅捷个人支付【已废弃】
     */
    QUICK_PAY_PERSONAL(8, "银行卡（个人）支付"),

    /**
     * 畅捷网银支付【已废弃】
     */
    QUICK_PAY_ENTERPRISE(9, "企业网银支付"),

    /**
     * 支付宝个人网银
     */
    PERSIONAL_EBANK(10, "支付宝个人网银"),

    /**
     * 支付宝企业网银
     */
    ENTERPRISE_EBANK(11, "支付宝企业网银"),

    /**
     * 余额支付
     */
    BALANCE_PAY(12, "余额支付");

    private Integer code;
    private String desc;

    private PaymentTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PaymentTypeEnum getByCode(Integer code) {
        for (PaymentTypeEnum item : PaymentTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static PaymentTypeEnum getByName(String name) {
        for (PaymentTypeEnum item : PaymentTypeEnum.values()) {
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
        }

        return null;
    }

    /**
     * CRM中只分为银行(1)和第三方(2)两种支付方式
     * '支付类型，1.支付宝收款,2.银行收款,3.银行提现,4.支付宝退款,
     * 5.银行退款,6微信支付，7微信退款，10支付宝网银个人，11支付宝网银企业',
     *
     * 第三方(2): 1 4 6 7
     *
     * @return
     */
    public static Integer getPaymentTypeDefinedInCrm(Integer type) {
        switch (type) {
            case 1:
            case 4:
            case 6:
            case 7:
                return 2;
            default:
                return 1;
        }
    }

    /***
     * 将CRM中的支付方式ID转化为名称
     */
    public static String getPaymentTypeNameInCrm(Integer type) {
        switch (type) {
            case 1:
                return "银行";
            case 2:
                return "第三方";
            default:
                return null;
        }
    }
}
