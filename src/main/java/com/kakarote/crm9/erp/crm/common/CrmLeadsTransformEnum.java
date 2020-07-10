package com.kakarote.crm9.erp.crm.common;

/**
 * CrmLeadsTransformEnum class
 *
 * @author yue.li
 * @date 2019/11/26
 */
public enum CrmLeadsTransformEnum {
    /**
     * CrmLeadsTransformEnum
     */
    LEADS_NOT_TRANSFORM_KEY("未转化", 0),
    LEADS_TRANSFORM_KEY("已转化", 1),
    LEADS_BACK_KEY("退回",2);

    private String name;
    private Integer types;
    CrmLeadsTransformEnum(String name, Integer types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(Integer types) {
        for (CrmLeadsTransformEnum crmLeadsTransformEnum : CrmLeadsTransformEnum.values()) {
            if (crmLeadsTransformEnum.getTypes().equals(types)) {
                return crmLeadsTransformEnum.name;
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

    public static Integer getTypeByName(String name) {
        CrmLeadsTransformEnum[] items = CrmLeadsTransformEnum.values();
        for(CrmLeadsTransformEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return null;
    }
}
