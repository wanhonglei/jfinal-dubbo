package com.kakarote.crm9.erp.crm.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回款核销信息
 * @author xiaowen.wu
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrmVerificationInfoDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6503968189434045929L;
	
	// 核销日期
	private Date verificationDate;

	// 核销金额
	private String verificationMoney;
	
	// 类型
	private String type;
	
	// 订单号
	private String orderNo;
	
	// 付款条款编号
	private String paymentCode;
}
