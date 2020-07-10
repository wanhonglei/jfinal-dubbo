package com.kakarote.crm9.erp.crm.entity;

import java.io.Serializable;
import java.util.List;

import com.kakarote.crm9.erp.crm.common.DistributorBdOrderEnum;
import com.kakarote.crm9.erp.crm.common.OrderTypeEnum;

import lombok.Data;

@Data
public class DistributorBdReport implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9200274240319768265L;

	private String saleAreaName;

	private String productName;
	
	private String customerName;
	private String productCode;
	private String saleAreaCode;
	private String deptId;
	private Long customerId;
	private Long bdCode;
	private String bdName;

	private List<OrderBy> orderBys;

	@Data
	public static class OrderBy {
		
		private DistributorBdOrderEnum orderKey;
		private OrderTypeEnum orderType;
	}
}
