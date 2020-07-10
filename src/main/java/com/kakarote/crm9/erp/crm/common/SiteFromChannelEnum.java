package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: honglei.wan
 * @Description:官网账号注册渠道枚举
 * @Date: Create in 2020/6/17 11:13 上午
 */
public enum  SiteFromChannelEnum {

	/**
	 * 官网账号注册渠道枚举
	 */
	PC(1,"PC"),
	MOBILE(2,"移动端"),
	IOS(3,"IOS"),
	ANDROID(4,"安卓"),
	WX_MINI(5,"微信小程序"),
	DD_MINI(6,"钉钉微应用"),
	E_CONTRACT(7,"电子合同"),
	;

	private Integer code;
	private String msg;

	SiteFromChannelEnum(Integer code,String msg){
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
}
