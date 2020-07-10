package com.kakarote.crm9.erp.crm.entity;

import java.io.Serializable;
import java.util.List;

import com.kakarote.crm9.erp.crm.common.DistributorSaleAreaOrderEnum;
import com.kakarote.crm9.erp.crm.common.OrderTypeEnum;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DistributorSaleAreaReport implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2794390147140641035L;
	
	
	private String saleAreaName;
	
	private String ProductName;
	private String productCode;
	private String saleAreaCode;
	private String deptId;

	private List<OrderBy> orderBys;

	@Data
	public static class OrderBy {
		
		private DistributorSaleAreaOrderEnum orderKey;
		private OrderTypeEnum orderType;
	}
}
