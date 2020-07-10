package com.kakarote.crm9.erp.crm.acl.dataauth;

/**
 * Crm Data Auth Enum
 *
 * @author yue.li
 */
public enum CrmDataAuthEnum {
    /**
     * CrmDataAuthEnum
     */
    ONESELF_TYPE_KEY("本人", 1),
    ONESELF_BRANCH_TYPE_KEY("本人及下属", 2),
    DEPARTMENT_TYPE_KEY("本部门", 3),
    DEPARTMENT_BRANCH_TYPE_KEY("本部门及下属部门", 4),
    ALL_TYPE_KEY("全部", 5),
    ;

    private String name;
    private Integer types;

    CrmDataAuthEnum(String name, Integer types) {
        this.name = name;
        this.types = types;
    }

    public static String getName(Integer types) {
        for (CrmDataAuthEnum c : CrmDataAuthEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return null;
    }

    public static CrmDataAuthEnum findByType(Integer types) {
        for (CrmDataAuthEnum value : values()) {
            if (value.getTypes().equals(types)) {
                return value;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public Integer getTypes() {
        return types;
    }

    public static Integer getTypeByName(String name) {
        CrmDataAuthEnum[] items = CrmDataAuthEnum.values();
        for (CrmDataAuthEnum item : items) {
            if (item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return null;
    }

    /**
     * Get enum by code.
     *
     * @param code enum code
     * @return enum
     */
    public static Enum getAuthTypeByCode(int code) {
        CrmDataAuthEnum[] items = CrmDataAuthEnum.values();
        for (CrmDataAuthEnum item : items) {
            if (item.getTypes() == code) {
                return item;
            }
        }
        return null;
    }
}
