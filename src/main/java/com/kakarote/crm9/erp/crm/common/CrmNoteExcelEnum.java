package com.kakarote.crm9.erp.crm.common;

public enum CrmNoteExcelEnum {
    /**
     * CrmNoteExcelEnum
     */
    NOTE_TYPE_KEY("类型", "noteTypes"),
    NOTE_NAME_KEY("名称", "noteName"),
    ADDRESS_NAME_KEY("省市区","address"),
    CREATE_TIME_KEY("记录日期","noteCreateTime"),
    CREATE_USER_NAME_KEY("记录人","realname"),
    CATEGORY_KEY("联系方式","category"),
    CONTENT_KEY("记录内容","content");

    private String name;
    private String types;
    CrmNoteExcelEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmNoteExcelEnum c : CrmNoteExcelEnum.values()) {
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
        CrmNoteExcelEnum[] items = CrmNoteExcelEnum.values();
        for(CrmNoteExcelEnum item : items) {
            if(item.getName().equals(name)) {
                return item.getTypes();
            }
        }
        return "";
    }
}
