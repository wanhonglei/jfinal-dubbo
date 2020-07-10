package com.kakarote.crm9.erp.crm.entity;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class CrmPlanReportResult {

    /**分页实体*/
    private Page<Record> pageResult;

    /**总计实际回款金额*/
    private String totalReceiveblesMoney;

    /**总计合同金额*/
    private String totalMoney;

    public Page<Record> getPageResult() {
        return pageResult;
    }

    public void setPageResult(Page<Record> pageResult) {
        this.pageResult = pageResult;
    }

    public String getTotalReceiveblesMoney() {
        return totalReceiveblesMoney;
    }

    public void setTotalReceiveblesMoney(String totalReceiveblesMoney) {
        this.totalReceiveblesMoney = totalReceiveblesMoney;
    }

    public String getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(String totalMoney) {
        this.totalMoney = totalMoney;
    }
}
