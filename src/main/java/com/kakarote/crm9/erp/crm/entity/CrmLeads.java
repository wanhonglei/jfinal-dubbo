package com.kakarote.crm9.erp.crm.entity;

import com.kakarote.crm9.erp.crm.entity.base.BaseCrmLeads;
import lombok.Data;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class CrmLeads extends BaseCrmLeads<CrmLeads> {
	public static final CrmLeads dao = new CrmLeads().dao();

	/**
	 * 精度需求名称
	 */
	private String accuracyRequirementsName;
	/**
	 * 客户等级名称
	 */
	private String customerLevelName;
	/**
	 * 客户行业名称
	 */
	private String customerIndustryName;
	/**
	 * 部门名称
	 */
	private String deptName;

	public String getAccuracyRequirementsName() {
		return accuracyRequirementsName;
	}

	public void setAccuracyRequirementsName(String accuracyRequirementsName) {
		this.accuracyRequirementsName = accuracyRequirementsName;
	}

	public String getCustomerLevelName() {
		return customerLevelName;
	}

	public void setCustomerLevelName(String customerLevelName) {
		this.customerLevelName = customerLevelName;
	}

	public String getCustomerIndustryName() {
		return customerIndustryName;
	}

	public void setCustomerIndustryName(String customerIndustryName) {
		this.customerIndustryName = customerIndustryName;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
}
