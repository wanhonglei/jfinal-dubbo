package com.kakarote.crm9.erp.crm.common;

/**
 * Crm Sign In Excel Enum
 *
 * @author hao.fu
 * @since 2019/11/27.
 */
public enum CrmSignInExcelEnum {

    /**
     * Sign in excel header
     */
    SIGNER("签到人", "signer"),
    CUSTOMER_NAME("客户名称", "clientName"),
    PROVINCE("省", "province"),
    CITY("市", "city"),
    DISTRICT("区", "district"),
    SIGN_IN_ADDRESS("签到地点", "location"),
    SIGN_IN_TIME("签到时间", "signinTime");

    private String name;
    private String types;

    CrmSignInExcelEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }

    public static String getName(String types) {
        for (CrmSignInExcelEnum c : CrmSignInExcelEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public String getTypes() {
        return types;
    }

    public static String getTypeByName(String name) {
        CrmSignInExcelEnum[] items = CrmSignInExcelEnum.values();
        for (CrmSignInExcelEnum item : items) {
            if (item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
