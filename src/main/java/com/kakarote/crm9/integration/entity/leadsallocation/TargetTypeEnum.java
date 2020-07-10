package com.kakarote.crm9.integration.entity.leadsallocation;

/**
 * 分发对象类型
 * @author xiaowen.wu
 *
 */
public enum TargetTypeEnum {

	USER("1","用户"),
	DEPT("2","部门");
	
    private String code;
    private String desc;

    private TargetTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TargetTypeEnum getByCode(String code) {
        for (TargetTypeEnum item : TargetTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static TargetTypeEnum getByName(String name) {
        for (TargetTypeEnum item : TargetTypeEnum.values()) {
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
        }

        return null;
    }
}
