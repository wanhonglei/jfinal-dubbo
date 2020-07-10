package com.kakarote.crm9.erp.crm.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/4/16 6:48 下午
 */

@Getter
@Setter
public class BaseRequestForBops {

	/**
	 * 调用方的工程名称
	 */

	private String caller;

	/**
	 * 参数对象
	 */

	private IParamForBops param;

	/**
	 * 分页对象
	 */

	private PageParamForBops paged;

	/**
	 * uuid生成随机数
	 */
	private String requestId;

	/**
	 * uuid生成随机数 或者为 null
	 */
	private String sessionRequestId;


	/**
	 * 分页对象
	 */

	@Getter
	@Setter
	public static class PageParamForBops {

		/**
		 * 当前页数（从1开始）
		 */

		private Integer pageNum;

		/**
		 * 每页大小
		 */

		private Integer pageSize;
	}
}
