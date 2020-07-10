package com.kakarote.crm9.erp.crm.common;

/**
 * CrmBusinessInformationExcelEnum class
 *
 * @author yue.li
 * @date 2020/04/08
 */
public enum CrmBusinessInformationExcelEnum {
    /**
     * CrmBusinessInformationExcelEnum
     */
    CRM_BUSINESS_NAME_KEY("商机名称", "businessName"),
    CRM_CUSTOMER_NAME_KEY("客户名称", "customerName"),
    CRM_CREATE_TIME_KEY("创建时间","createTime"),
    CRM_UPDATE_TIME_KEY("修改时间","updateTime"),
    CRM_OWNER_USER_NAME_KEY("负责人","ownerUserName"),
    CRM_CREATE_USER_NAME_KEY("创建人","createUserName");


    private String name;
    private String types;
    CrmBusinessInformationExcelEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmBusinessInformationExcelEnum crmBusinessInformationExcelEnum : CrmBusinessInformationExcelEnum.values()) {
            if (crmBusinessInformationExcelEnum.getTypes().equals(types)) {
                return crmBusinessInformationExcelEnum.name;
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
        CrmBusinessInformationExcelEnum[] items = CrmBusinessInformationExcelEnum.values();
        for(CrmBusinessInformationExcelEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
