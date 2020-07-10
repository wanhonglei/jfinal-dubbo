package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: honglei.wan
 * @Description:crm事件枚举
 * @Date: Create in 2020/5/15 1:53 下午
 */
public enum CrmEventEnum {
	/**
	 * CrmEventEnum
	 */
	LATELY_FOLLOW_EVENT("最近跟进事件", "LATELY_FOLLOW_EVENT"),
	;

	private String name;
	private String types;
	CrmEventEnum(String name, String types) {
		this.name = name;
		this.types = types;
	}
	public static String getName(String types) {
		for (CrmEventEnum c : CrmEventEnum.values()) {
			if (c.getTypes().equals(types)) {
				return c.name;
			}
		}
		return null;
	}
	public String getName() {
		return name;
	}

	public String getTypes() {
		return types;
	}
}
