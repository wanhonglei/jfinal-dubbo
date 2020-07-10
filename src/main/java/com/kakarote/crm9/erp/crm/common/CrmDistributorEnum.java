package com.kakarote.crm9.erp.crm.common;
/**
 * CrmDistributorEnum class
 *
 * @author yue.li
 * @date 2019/12/18
 */
public enum CrmDistributorEnum {

    /**
     * CrmDistributorEnum
     */
    IS_DISTRIBUTOR("分销商", "1"),
    IS_NOT_DISTRIBUTOR("不是分销商", "2");

    private String name;
    private String types;
    CrmDistributorEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmDistributorEnum c : CrmDistributorEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return null;
    }
    public String getName() {
        return name;
    }

    public String getTypes() {
        return types;
    }

}
