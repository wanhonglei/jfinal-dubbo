package com.kakarote.crm9.erp.crm.vo;

import lombok.Data;

/**
 * @Author: honglei.wan
 * @Description: 客户引导查询对象
 * @Date: Create in 2020/5/11 2:44 下午
 */
@Data
public class CrmCustomerGuideQueryVO {
	/**
	 * 客户名称
	 */
	private String customerName;

	/**
	 * 官网客户id
	 */
	private String siteMemberId;

	/**
	 * 官网用户名
	 */
	private String siteMemberName;

	/**
	 * 手机号
	 */
	private String mobile;
}
