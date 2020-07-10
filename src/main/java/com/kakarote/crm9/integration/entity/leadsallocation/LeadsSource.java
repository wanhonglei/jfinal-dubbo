package com.kakarote.crm9.integration.entity.leadsallocation;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
/**
 * 分发资源
 * @author xiaowen.wu
 *
 * @param <T>
 */
@Data
public class LeadsSource<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4070327199626707556L;
	
	// 资源类型
	private SourceTypeEnum sourceType;
	
	// 资源id
	private Long sourceId;
	
	// 资源
	private List<T> sources;
	
}
