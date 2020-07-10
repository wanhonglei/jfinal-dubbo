package com.kakarote.crm9.erp.crm.common;
/**
 * CrmNotesChannelEnum class
 *
 * @author yue.li
 * @date 2020/01/17
 */
public enum CrmNotesChannelEnum {
    /**
     * CrmNotesChannelEnum
     */
    PC_CHANNEL_KEY("PC端", 1),
    MOBILE_CHANNEL_KEY("移动端", 2);

    private String name;
    private Integer types;

    CrmNotesChannelEnum(String name, Integer types) {
        this.name = name;
        this.types = types;
    }

    public static String getName(Integer types) {
        for (CrmNotesChannelEnum c : CrmNotesChannelEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return "";
    }

    public String getName() {
        return name;
    }


    public Integer getTypes() { return types; }
}


