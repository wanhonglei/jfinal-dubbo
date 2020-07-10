package com.kakarote.crm9.erp.crm.entity;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class CrmProductReceivablesReportResult {


    /**分页实体*/
    private Page<Record> pageResult;

    /**总计实际回款金额*/
    private String totalReceivablesMoney;

    /**总计合同金额*/
    private String totalReceivablesPlanMoney;

	public Page<Record> getPageResult() {
		return pageResult;
	}

	public void setPageResult(Page<Record> pageResult) {
		this.pageResult = pageResult;
	}

	public String getTotalReceivablesMoney() {
		return totalReceivablesMoney;
	}

	public void setTotalReceivablesMoney(String totalReceivablesMoney) {
		this.totalReceivablesMoney = totalReceivablesMoney;
	}

	public String getTotalReceivablesPlanMoney() {
		return totalReceivablesPlanMoney;
	}

	public void setTotalReceivablesPlanMoney(String totalReceivablesPlanMoney) {
		this.totalReceivablesPlanMoney = totalReceivablesPlanMoney;
	}
    
}
