package com.kakarote.crm9.erp.crm.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DistributorBdSalesQuantityReportResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2633354689784862091L;
	
	// 时间
	private Date gmtCreate;

	// 分销商（客户）名称
	private String customerName;

	// 客户id
	private String customerId;

	// 负责bd名称
	private String bdName;

	// 负责bd
	private String bdCode;

	// 销售区域名称
	private String saleAreaName;
	
	// 销售区域code
	private String saleAreaCode;
	
	// 产品名称（对应多个商品）
	private String productName;
	
	// 商品规格
	private String goodsSpec;
	
	// 操作类型 0-增加 1减少
	private Integer operationType;
	
	// 操作类型名称
	private String operationTypeName;
	
	// 销售数量
	private Long bdSalesQuantity; 

	// 减少原因
	private Integer reduceReasons;

	// 减少原因名称
	private String reduceReasonsName;
	
	// 备注
	private String remark;

}
