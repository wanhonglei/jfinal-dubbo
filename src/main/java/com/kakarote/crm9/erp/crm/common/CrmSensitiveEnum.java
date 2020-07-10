package com.kakarote.crm9.erp.crm.common;

/**
 * Sensitive data of CRM.
 *
 * @author hao.fu
 * @create 2019/8/6 13:35
 */
public enum CrmSensitiveEnum {

    /**
     * 联系人-电话
     */
    CONTACTS_TELEPHONE("72crm_crm_contacts", "telephone"),

    /**
     * 联系人-手机
     */
    CONTACTS_MOBILE("72crm_crm_contacts", "mobile"),

    /**
     * 联系人-微信
     */
    CONTACTS_WECHAT("72crm_crm_contacts", "wechat"),

    /**
     * 联系人-邮箱
     */
    CONTACTS_EMAIL("72crm_crm_contacts", "email"),

    /**
     * 线索-电话
     */
    LEADS_TELEPHONE("72crm_crm_leads", "telephone"),

    /**
     * 线索-微信
     */
    LEADS_WECHAT("72crm_crm_leads", "we_chat"),

    /**
     * 线索-邮箱
     */
    LEADS_EMAIL("72crm_crm_leads", "email");

    private String tableName;
    private String fieldName;

    CrmSensitiveEnum(String tableName, String fieldName) {
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
