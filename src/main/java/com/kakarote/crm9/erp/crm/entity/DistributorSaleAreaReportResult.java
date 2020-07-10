package com.kakarote.crm9.erp.crm.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class DistributorSaleAreaReportResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5640589443074369462L;
	
	// 大区
	private Long deptId;
	
	// 大区名称
	private String deptName;
	
	// 销售区域名称
	private String saleAreaName;
	
	// 销售区域code
	private String saleAreaCode;
	
	// 产品名称（对应多个商品）
	private String productName;
	
	// 商品规格
	private String goodsSpec;
	
	// 订购数量（72crm_distributor_statistic.sales_quantity）
	private Long sumSalesQuantity;
	
	// 发货数量
	private Long sumDeliveryQuantity;

	// 销售数量(72crm_crm_customer_sales_log.sales_quantity)
	private Long bdSalesQuantity;
	
	// 理论库存
	private Long theoreticalInventory;
	
	// 实际库存
	private Long actualInventory;
}
