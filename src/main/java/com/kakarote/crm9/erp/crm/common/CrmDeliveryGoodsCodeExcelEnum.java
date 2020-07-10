package com.kakarote.crm9.erp.crm.common;

/**
 * CrmDeliveryGoodsCodeExcelEnum class
 *
 * @author yue.li
 * @date 2019/11/29
 */
public enum CrmDeliveryGoodsCodeExcelEnum {
    /**
     * CrmDeliveryGoodsCodeExcelEnum
     */
    CRM_GOODS_NAME_KEY("商品名称", "goodsName"),
    CRM_GOODS_SPEC_KEY("商品SKU", "goodsSpec"),
    CRM_GOODS_CODE_KEY("商品code","goodsCode");


    private String name;
    private String types;
    CrmDeliveryGoodsCodeExcelEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmDeliveryGoodsCodeExcelEnum crmDeliveryInformationExcelEnum : CrmDeliveryGoodsCodeExcelEnum.values()) {
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
        CrmDeliveryGoodsCodeExcelEnum[] items = CrmDeliveryGoodsCodeExcelEnum.values();
        for(CrmDeliveryGoodsCodeExcelEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
