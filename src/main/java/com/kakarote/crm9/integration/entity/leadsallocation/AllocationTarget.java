package com.kakarote.crm9.integration.entity.leadsallocation;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分发对象
 * @author xiaowen.wu
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocationTarget implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2049021797656709831L;

	// 对象类型
	private TargetTypeEnum targetType;
	
	// 对象id
	private Long targetId;
	
	// 对象名称
	private String targetName;

	/**
	 * 	每次阈值
	 */
	private Long onceThreshold;

	/**
	 * 每日阈值
	 */
	private Long dayThreshold;

	/**
	 * 当前可用阈值
	 */
	private Long usefulThreshold;
	
}
