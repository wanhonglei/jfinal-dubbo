package com.kakarote.crm9.erp.crm.vo;

import lombok.Data;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/18 11:42 上午
 */
@Data
public class CustomerQuerySceneVO {
	/**
	 * 场景名称
	 */
	private String sceneName;

	/**
	 * 场景 Code
	 */
	private String sceneCode;

	/**
	 * 默认展示
	 */
	private Integer isDefault;
}
