package com.kakarote.crm9.erp.crm.common;

/**
 * CrmLeadsSendTimesEnum class
 *
 * @author yue.li
 * @date 2019/11/26
 */
public enum CrmLeadsSendTimesEnum {
    /**
     * CrmLeadsSendTimesEnum
     */
    LEADS_SEND_EMAIL_ONE_TIMES_KEY("线索发送过一次", 1),
    LEADS_SEND_EMAIL_TWO_TIMES_KEY("线索发送过两次",2),
    LEADS_SEND_EMAIL_THREE_TIMES_KEY("线索发送过三次",3);
    private String name;
    private Integer types;
    CrmLeadsSendTimesEnum(String name, Integer types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(Integer types) {
        for (CrmLeadsSendTimesEnum crmLeadsSendTimesEnum : CrmLeadsSendTimesEnum.values()) {
            if (crmLeadsSendTimesEnum.getTypes().equals(types)) {
                return crmLeadsSendTimesEnum.name;
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
        CrmLeadsSendTimesEnum[] items = CrmLeadsSendTimesEnum.values();
        for(CrmLeadsSendTimesEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return null;
    }
}
