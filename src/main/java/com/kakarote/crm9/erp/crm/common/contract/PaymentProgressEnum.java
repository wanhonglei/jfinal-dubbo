package com.kakarote.crm9.erp.crm.common.contract;

/**
 * 合同回款进度
 * @Author: haihong.wu
 * @Date: 2020/6/15 4:19 下午
 */
public enum PaymentProgressEnum {
    ALL_PAID("全部回款"),
    PARTIAL_PAID("部分回款"),
    UN_PAID("未回款"),
    ;

    private String desc;

    PaymentProgressEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
