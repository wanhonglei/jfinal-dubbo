package com.kakarote.crm9.erp.crm.common;

/**
 * CrmDeliveryInformationExcelEnum class
 *
 * @author yue.li
 * @date 2019/11/29
 */
public enum CrmDeliveryInformationExcelEnum {
    /**
     * CrmDeliveryInformationExcelEnum
     */
    CRM_ORDER_NO_KEY("订单编号", "orderNo"),
    CRM_EXPRESS_COMPANY_KEY("快递公司", "expressCompany"),
    CRM_EXPRESS_NO_KEY("快递单号","expressNo"),
    CRM_GOODS_NAME_KEY("商品名称","goodsName"),
    CRM_GOODS_SPEC_KEY("商品规格","goodsSpec"),
    CRM_HARDWARE_SN_NO_KEY("SN编号","hardwareSnNo"),
    CRM_NUM_KEY("发货数量","num");


    private String name;
    private String types;
    CrmDeliveryInformationExcelEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmDeliveryInformationExcelEnum crmDeliveryInformationExcelEnum : CrmDeliveryInformationExcelEnum.values()) {
            if (crmDeliveryInformationExcelEnum.getTypes().equals(types)) {
                return crmDeliveryInformationExcelEnum.name;
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
        CrmDeliveryInformationExcelEnum[] items = CrmDeliveryInformationExcelEnum.values();
        for(CrmDeliveryInformationExcelEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
