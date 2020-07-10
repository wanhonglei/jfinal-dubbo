package com.kakarote.crm9.erp.crm.entity;

import com.jfinal.plugin.activerecord.Record;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * Business Receivable Plan Result
 *
 * @author hao.fu
 * @create 2019/10/9 16:01
 */
public class BusinessReceivablePlanResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6275925862068207904L;

	// 回款计划列表
    List<Record> receivablePlans;

    // 计划回款合计
    BigDecimal planTotalMoney;

    // 与商机金额相差
    BigDecimal businessBalance;
    
    //已关联合同回款金额
    private BigDecimal releativeContractMoney;
    
    //未关联合同回款金额
    private BigDecimal notReleativeContractMoney;

    public List<Record> getReceivablePlans() {
        return receivablePlans;
    }

    public void setReceivablePlans(List<Record> receivablePlans) {
        this.receivablePlans = receivablePlans;
    }

    public BigDecimal getPlanTotalMoney() {
        return planTotalMoney;
    }

    public void setPlanTotalMoney(BigDecimal planTotalMoney) {
        this.planTotalMoney = planTotalMoney;
    }

    public BigDecimal getBusinessBalance() {
        return businessBalance;
    }

    public void setBusinessBalance(BigDecimal businessBalance) {
        this.businessBalance = businessBalance;
    }

	public BigDecimal getReleativeContractMoney() {
		return releativeContractMoney;
	}

	public void setReleativeContractMoney(BigDecimal releativeContractMoney) {
		this.releativeContractMoney = releativeContractMoney;
	}

	public BigDecimal getNotReleativeContractMoney() {
		return notReleativeContractMoney;
	}

	public void setNotReleativeContractMoney(BigDecimal notReleativeContractMoney) {
		this.notReleativeContractMoney = notReleativeContractMoney;
	}
    
}
