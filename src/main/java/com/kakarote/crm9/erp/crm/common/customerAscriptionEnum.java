package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: honglei.wan
 * @Description:客户归属枚举类
 * @Date: Create in 2020/5/14 7:37 下午
 */
public enum customerAscriptionEnum {
	/**
	 * 客户归属枚举类
	 */
	CUSTOMER_POOL_OUT("网站客户池(保护期外)", 1),
	CUSTOMER_POOL_IN("网站客户池(保护期内)", 2),
	DEPT_POOL("部门客户池", 3),
	TELEMARKETING_POOL("电销客户", 4),
	INDUSTRY_BD("行业BD", 5),
	;

	private String name;
	private Integer code;
	customerAscriptionEnum(String name, Integer code) {
		this.name = name;
		this.code = code;
	}
	public String getName() {
		return name;
	}

	public Integer getCode() {
		return code;
	}
}
