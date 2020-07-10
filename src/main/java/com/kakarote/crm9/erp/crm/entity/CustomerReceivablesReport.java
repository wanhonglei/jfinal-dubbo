package com.kakarote.crm9.erp.crm.entity;

public class CustomerReceivablesReport {

    /**部门id*/
    public String deptId;

    /**客户名称*/
    public String customerName;

    /**计划回款开始日期*/
    public String receivablesPlanStartTime;

    /**计划回款结束日期*/
    public String receivablesPlanEndTime;

    /**赢率*/
    public String winRate;

    /**实际回款开始时间*/
    public String receivablesStartTime;

    /**实际回款结束时间*/
    public String receivablesEndTime;

    /**BD*/
    public String ownerUserName;

    /**BD*/
    public String ownerUserId;

	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getReceivablesPlanStartTime() {
		return receivablesPlanStartTime;
	}

	public void setReceivablesPlanStartTime(String receivablesPlanStartTime) {
		this.receivablesPlanStartTime = receivablesPlanStartTime;
	}

	public String getReceivablesPlanEndTime() {
		return receivablesPlanEndTime;
	}

	public void setReceivablesPlanEndTime(String receivablesPlanEndTime) {
		this.receivablesPlanEndTime = receivablesPlanEndTime;
	}

	public String getWinRate() {
		return winRate;
	}

	public void setWinRate(String winRate) {
		this.winRate = winRate;
	}

	public String getReceivablesStartTime() {
		return receivablesStartTime;
	}

	public void setReceivablesStartTime(String receivablesStartTime) {
		this.receivablesStartTime = receivablesStartTime;
	}

	public String getReceivablesEndTime() {
		return receivablesEndTime;
	}

	public void setReceivablesEndTime(String receivablesEndTime) {
		this.receivablesEndTime = receivablesEndTime;
	}

	public String getOwnerUserName() {
		return ownerUserName;
	}

	public void setOwnerUserName(String ownerUserName) {
		this.ownerUserName = ownerUserName;
	}

	public String getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}
    
}
