package com.kakarote.crm9.erp.crm.entity;

import java.io.Serializable;
import java.util.List;

import com.kakarote.crm9.erp.crm.common.DistributorBdSalesQuantityOrderEnum;
import com.kakarote.crm9.erp.crm.common.OrderTypeEnum;

import lombok.Data;

@Data
public class DistributorBdSalesQuantityReport implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 127901901781581135L;

	private String saleAreaName;
	private String saleAreaCode;

	private String productName;
	
	private String customerName;
	private String productCode;
	private Long deptId;
	private Long customerId;
	private Long bdCode;
	private String bdName;
	private Integer operationType;
	private String salesStartTime;
	private String salesEndTime;

	private List<OrderBy> orderBys;

	@Data
	public static class OrderBy {
		
		private DistributorBdSalesQuantityOrderEnum orderKey;
		private OrderTypeEnum orderType;
	}
}
