package com.kakarote.crm9.erp.crm.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/16 11:36 上午
 */
@Data
public class CustomerWithUserNameVO {
	/**
	 * 客户id
	 */
	private Long customerId;

	/**
	 * 客户姓名
	 */
	private String customerName;

	/**
	 * 负责人id
	 */
	private Integer ownerUserId;

	/**
	 * 负责人姓名
	 */
	private String ownerUserName;

	/**
	 * 客户归属部门id
	 */
	private Integer deptId;

	/**
	 * 客户归属部门名称
	 */
	private String deptName;

	/**
	 * 官网用户列表
	 */
	private List<SiteMemberInfo> siteMemberList;

	@Data
	public static class SiteMemberInfo{
		/**
		 * 官网用户ID
		 */
		private Integer siteMemberId;

		/**
		 * 官网用户姓名
		 */
		private String  siteMemberName;
	}

}





