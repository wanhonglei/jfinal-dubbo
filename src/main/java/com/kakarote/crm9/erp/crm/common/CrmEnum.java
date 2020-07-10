package com.kakarote.crm9.erp.crm.common;

import org.apache.commons.lang3.StringUtils;

/**
 * Crm enum.
 *
 * @author hao.fu
 */
public enum CrmEnum {
    /**
     * CRM Enum
     */
    LEADS_TYPE_KEY("线索", "1"),
    CUSTOMER_TYPE_KEY("客户", "2"),
    CONTACTS_TYPE_KEY("联系人", "3"),
    PRODUCT_TYPE_KEY("产品", "4"),
    BUSINESS_TYPE_KEY("商机", "5"),
    CONTRACT_TYPE_KEY("合同", "6"),
    RECEIVABLES_TYPE_KEY("回款", "7"),
    WEBSITE_POOL("网站客户池", "8"),
    RECEIVABLES_PLAN_TYPE_KEY("回款计划", "9"),
    CUSTOMER_BASE_INFO("客户基本信息", "11"),
    ADMIN_FIELD_TYPE_KEY("系统自定义字段","10"),
    RECEIVABLES_MANAGEMENT_KEY("回款管理","12"),
    CUSTOMER_DISTRIBUTE_KEY("客户资源分发", "13"),
    CUSTOMER_UPLOAD_BY_EXCEL("客户excel导入","14"),
    DISTRIBUTE_KEY("分销商", "15"),
    ;

    private String name;
    private String types;

    CrmEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }

    public static String getName(String types) {
        for (CrmEnum c : CrmEnum.values()) {
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
        CrmEnum[] items = CrmEnum.values();
        for (CrmEnum item : items) {
            if (item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }

    public static CrmEnum getByType(String type) {
        for (CrmEnum value : values()) {
            if (StringUtils.equals(value.getTypes(), type)) {
                return value;
            }
        }
        return null;
    }
}
