package com.kakarote.crm9.erp.crm.entity;

import com.kakarote.crm9.erp.crm.entity.base.BaseCrmReceivablesAbnormalFunds;

/**
 * Generated by JFinal.
 * @author haihong.wu
 */
@SuppressWarnings("serial")
public class CrmReceivablesAbnormalFunds extends BaseCrmReceivablesAbnormalFunds<CrmReceivablesAbnormalFunds> {
	public static final CrmReceivablesAbnormalFunds dao = new CrmReceivablesAbnormalFunds().dao();

	public CrmReceivablesAbnormalFunds getByPaymentNo(String paymentNo) {
		return dao.findSingleByColumn("payment_no", paymentNo);
	}
}
