package com.kakarote.crm9.erp.crm.entity.base;

import com.jfinal.plugin.activerecord.CrmModel;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseCrmReceivablesPlan<M extends BaseCrmReceivablesPlan<M>> extends CrmModel<M> implements IBean {

	public M setPlanId(Long planId) {
		set("plan_id", planId);
		return (M)this;
	}
	
	public Long getPlanId() {
		return getLong("plan_id");
	}

	/**
	 * 期数
	 */
	public M setNum(String num) {
		set("num", num);
		return (M)this;
	}
	
	/**
	 * 期数
	 */
	public String getNum() {
		return getStr("num");
	}

	/**
	 * 回款ID
	 */
	public M setReceivablesId(Integer receivablesId) {
		set("receivables_id", receivablesId);
		return (M)this;
	}
	
	/**
	 * 回款ID
	 */
	public Integer getReceivablesId() {
		return getInt("receivables_id");
	}

	/**
	 * 1完成 0 未完成
	 */
	public M setStatus(Integer status) {
		set("status", status);
		return (M)this;
	}
	
	/**
	 * 1完成 0 未完成
	 */
	public Integer getStatus() {
		return getInt("status");
	}

	/**
	 * 计划回款金额
	 */
	public M setMoney(java.math.BigDecimal money) {
		set("money", money);
		return (M)this;
	}
	
	/**
	 * 计划回款金额
	 */
	public java.math.BigDecimal getMoney() {
		return get("money");
	}

	/**
	 * 计划回款日期
	 */
	public M setReturnDate(java.util.Date returnDate) {
		set("return_date", returnDate);
		return (M)this;
	}
	
	/**
	 * 计划回款日期
	 */
	public java.util.Date getReturnDate() {
		return get("return_date");
	}

	/**
	 * 计划回款方式
	 */
	public M setReturnType(String returnType) {
		set("return_type", returnType);
		return (M)this;
	}
	
	/**
	 * 计划回款方式
	 */
	public String getReturnType() {
		return getStr("return_type");
	}

	/**
	 * 提前几天提醒
	 */
	public M setRemind(Integer remind) {
		set("remind", remind);
		return (M)this;
	}
	
	/**
	 * 提前几天提醒
	 */
	public Integer getRemind() {
		return getInt("remind");
	}

	/**
	 * 提醒日期
	 */
	public M setRemindDate(java.util.Date remindDate) {
		set("remind_date", remindDate);
		return (M)this;
	}
	
	/**
	 * 提醒日期
	 */
	public java.util.Date getRemindDate() {
		return get("remind_date");
	}

	/**
	 * 备注
	 */
	public M setRemark(String remark) {
		set("remark", remark);
		return (M)this;
	}
	
	/**
	 * 备注
	 */
	public String getRemark() {
		return getStr("remark");
	}

	/**
	 * 创建人ID
	 */
	public M setCreateUserId(Integer createUserId) {
		set("create_user_id", createUserId);
		return (M)this;
	}
	
	/**
	 * 创建人ID
	 */
	public Integer getCreateUserId() {
		return getInt("create_user_id");
	}

	/**
	 * 负责人ID
	 */
	public M setOwnerUserId(Integer ownerUserId) {
		set("owner_user_id", ownerUserId);
		return (M)this;
	}
	
	/**
	 * 负责人ID
	 */
	public Integer getOwnerUserId() {
		return getInt("owner_user_id");
	}

	/**
	 * 创建时间
	 */
	public M setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
		return (M)this;
	}
	
	/**
	 * 创建时间
	 */
	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	/**
	 * 修改时间
	 */
	public M setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
		return (M)this;
	}
	
	/**
	 * 修改时间
	 */
	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

	/**
	 * 附件批次ID
	 */
	public M setFileBatch(String fileBatch) {
		set("file_batch", fileBatch);
		return (M)this;
	}
	
	/**
	 * 附件批次ID
	 */
	public String getFileBatch() {
		return getStr("file_batch");
	}

	/**
	 * 合同ID
	 */
	public M setContractId(Integer contractId) {
		set("contract_id", contractId);
		return (M)this;
	}
	
	/**
	 * 合同ID
	 */
	public Integer getContractId() {
		return getInt("contract_id");
	}

	/**
	 * 客户ID
	 */
	public M setCustomerId(Integer customerId) {
		set("customer_id", customerId);
		return (M)this;
	}
	
	/**
	 * 客户ID
	 */
	public Integer getCustomerId() {
		return getInt("customer_id");
	}

	/**
	 * 商机id
	 */
	public M setBusinessId(Integer businessId) {
		set("business_id", businessId);
		return (M)this;
	}
	
	/**
	 * 商机id
	 */
	public Integer getBusinessId() {
		return getInt("business_id");
	}

	/**
	 * 赢率
	 */
	public M setWinRate(String winRate) {
		set("win_rate", winRate);
		return (M)this;
	}
	
	/**
	 * 赢率
	 */
	public String getWinRate() {
		return getStr("win_rate");
	}

	/**
	 * 失败原因
	 */
	public M setLoseReason(String loseReason) {
		set("lose_reason", loseReason);
		return (M)this;
	}
	
	/**
	 * 失败原因
	 */
	public String getLoseReason() {
		return getStr("lose_reason");
	}

	/**
	 * 付款条件
	 */
	public M setPaymentCondition(String paymentCondition) {
		set("payment_condition", paymentCondition);
		return (M)this;
	}
	
	/**
	 * 付款条件
	 */
	public String getPaymentCondition() {
		return getStr("payment_condition");
	}

	/**
	 * 数据所属人id
	 */
	public M setAuthUserId(Integer authUserId) {
		set("auth_user_id", authUserId);
		return (M)this;
	}
	
	/**
	 * 数据所属人id
	 */
	public Integer getAuthUserId() {
		return getInt("auth_user_id");
	}

	/**
	 * 数据所属部门id
	 */
	public M setAuthDeptId(Integer authDeptId) {
		set("auth_dept_id", authDeptId);
		return (M)this;
	}
	
	/**
	 * 数据所属部门id
	 */
	public Integer getAuthDeptId() {
		return getInt("auth_dept_id");
	}

}
