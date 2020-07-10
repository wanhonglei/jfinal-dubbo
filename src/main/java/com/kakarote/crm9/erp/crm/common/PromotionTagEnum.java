package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/26 4:13 下午
 */
public enum PromotionTagEnum {
    Distributor("distributor", "分销商"),
    TerminalUser("terminalUser", "终端用户"),
    DistributorL1("distributorPartnerL1", "一级分销商"),
    DistributorL2("distributorPartnerL2", "二级分销商"),
    DistributorL3("distributorPartnerL3", "三级分销商"),
    DistributorL4("distributorPartnerL4", "四级分销商"),
    ;

    private String code;
    private String desc;

    public static String getDesc(String code) {
        for (PromotionTagEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getDesc();
            }
        }
        return null;
    }

    PromotionTagEnum(String code, String desc) {
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
