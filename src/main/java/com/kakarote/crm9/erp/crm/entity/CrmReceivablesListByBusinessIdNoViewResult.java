package com.kakarote.crm9.erp.crm.entity;

import java.util.List;

import com.jfinal.plugin.activerecord.Record;

public class CrmReceivablesListByBusinessIdNoViewResult {

	private List<Record> receivables;
	
	/**
	 * 实际回款总计
	 */
	private String totalMoney;
	
	/**
	 * 与计划回款相差
	 */
	private String businessBalance;

	public List<Record> getReceivables() {
		return receivables;
	}

	public void setReceivables(List<Record> receivables) {
		this.receivables = receivables;
	}

	public String getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(String totalMoney) {
		this.totalMoney = totalMoney;
	}

	public String getBusinessBalance() {
		return businessBalance;
	}

	public void setBusinessBalance(String businessBalance) {
		this.businessBalance = businessBalance;
	}
	
}
