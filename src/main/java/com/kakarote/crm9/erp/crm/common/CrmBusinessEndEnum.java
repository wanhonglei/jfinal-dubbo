package com.kakarote.crm9.erp.crm.common;

/**
 * CrmBusinessEndEnum class
 *
 * @author yue.li
 * @date 2020/04/22
 */
public enum CrmBusinessEndEnum {
    /**
     * CrmBusinessEndEnum
     */
    WIN_TYPE_KEY("赢单", 1),
    LOSE_TYPE_KEY("输单", 2);

    private String name;
    private Integer types;
    CrmBusinessEndEnum(String name, Integer types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(Integer types) {
        for (CrmBusinessEndEnum c : CrmBusinessEndEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public Integer getTypes() {
        return types;
    }

}
