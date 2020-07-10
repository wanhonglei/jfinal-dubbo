package com.kakarote.crm9.erp.crm.common;

public enum CrmSalesOperationTypeEnum {

    INCREASE(0, "增加销售数量"),
    REDUCE(1, "减少销售数量");
	
    private Integer code;
    private String type;
    CrmSalesOperationTypeEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    public static Integer getCodeByType(String type) {
    	CrmSalesOperationTypeEnum[] items = CrmSalesOperationTypeEnum.values();
        for(CrmSalesOperationTypeEnum item : items) {
            if(item.getType().equals(type)) {
                return item.getCode();
            }
        }
        return null;
    }

    public static String getTypeByCode(Integer code) {
    	CrmSalesOperationTypeEnum[] items = CrmSalesOperationTypeEnum.values();
        for(CrmSalesOperationTypeEnum item : items) {
            if(item.getCode().equals(code)) {
                return item.getType();
            }
        }
        return null;
    }
}
