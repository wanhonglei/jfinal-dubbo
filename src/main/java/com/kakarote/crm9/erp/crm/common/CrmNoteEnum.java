package com.kakarote.crm9.erp.crm.common;

public enum CrmNoteEnum {
    /**
     * CrmNoteEnum
     */
    CRM_BUSINESS_KEY("商机", "crm_business"),
    CRM_LEADS_KEY("线索", "crm_leads"),
    CRM_CUSTOMER_KEY("客户","crm_customer"),
    CRM_CONTACTS_KEY("联系","crm_contacts");

    private String name;
    private String types;
    CrmNoteEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmNoteEnum c : CrmNoteEnum.values()) {
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
        CrmNoteEnum[] items = CrmNoteEnum.values();
        for(CrmNoteEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
