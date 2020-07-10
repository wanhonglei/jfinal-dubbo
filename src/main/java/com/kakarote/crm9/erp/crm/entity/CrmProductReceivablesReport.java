package com.kakarote.crm9.erp.crm.entity;

public class CrmProductReceivablesReport {

    /**部门id*/
    public String deptId;

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

    /**应用场景*/
    public String scenarioId;
    
    /**商品类别*/
    public String categoryId;

	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
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

	public String getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

}
