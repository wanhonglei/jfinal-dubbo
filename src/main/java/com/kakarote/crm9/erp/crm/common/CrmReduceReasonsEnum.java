package com.kakarote.crm9.erp.crm.common;

public enum CrmReduceReasonsEnum {

    RETURN_STOCK(1, "退库"),
    FILL_ERROR(2,"填写错误"),
    OTHERS(3,"其他");

    private Integer code;
    private String name;
    CrmReduceReasonsEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public static Integer getCode(String name) {
        for (CrmReduceReasonsEnum c : CrmReduceReasonsEnum.values()) {
            if (c.getName().equals(name)) {
                return c.code;
            }
        }
        return null;
    }
    public static String getName(Integer code) {
        for (CrmReduceReasonsEnum c : CrmReduceReasonsEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.name;
            }
        }
        return "";
    }
    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static Integer getTypeByName(String name) {
        CrmReduceReasonsEnum[] items = CrmReduceReasonsEnum.values();
        for(CrmReduceReasonsEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getCode();
            }
        }
        return null;
    }
}
