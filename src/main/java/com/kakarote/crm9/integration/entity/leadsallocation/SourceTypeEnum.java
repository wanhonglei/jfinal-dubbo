package com.kakarote.crm9.integration.entity.leadsallocation;

/**
 * 资源类型
 * @author xiaowen.wu
 *
 */
public enum SourceTypeEnum {

	CUSTOMER("1","客户"),
	CLUE("2","线索");
	
    private String code;
    private String desc;

    private SourceTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SourceTypeEnum getByCode(String code) {
        for (SourceTypeEnum item : SourceTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static SourceTypeEnum getByName(String name) {
        for (SourceTypeEnum item : SourceTypeEnum.values()) {
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
        }

        return null;
    }
}
