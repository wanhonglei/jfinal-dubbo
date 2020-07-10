package com.kakarote.crm9.integration.common;

public enum ThirdCustomerOriginEnum {

	SITE_MEMBER("1","网站会员"),
	DISTRIBUTOR_AUDIT("2","分销商认证");
	
    private String code;
    private String desc;

    private ThirdCustomerOriginEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ThirdCustomerOriginEnum getByCode(String code) {
        for (ThirdCustomerOriginEnum item : ThirdCustomerOriginEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static ThirdCustomerOriginEnum getByName(String name) {
        for (ThirdCustomerOriginEnum item : ThirdCustomerOriginEnum.values()) {
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
        }

        return null;
    }
}
