package com.kakarote.crm9.erp.crm.entity.base;

import com.jfinal.plugin.activerecord.CrmModel;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseCrmBusinessStatus<M extends BaseCrmBusinessStatus<M>> extends CrmModel<M> implements IBean {

	public M setStatusId(Long statusId) {
		set("status_id", statusId);
		return (M)this;
	}
	
	public Long getStatusId() {
		return getLong("status_id");
	}

	public M setTypeId(Integer typeId) {
		set("type_id", typeId);
		return (M)this;
	}
	
	public Integer getTypeId() {
		return getInt("type_id");
	}

	public M setName(String name) {
		set("name", name);
		return (M)this;
	}
	
	public String getName() {
		return getStr("name");
	}

	public M setRate(String rate) {
		set("rate", rate);
		return (M)this;
	}
	
	public String getRate() {
		return getStr("rate");
	}

	public M setOrderNum(Integer orderNum) {
		set("order_num", orderNum);
		return (M)this;
	}
	
	public Integer getOrderNum() {
		return getInt("order_num");
	}

	public M setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
		return (M)this;
	}
	
	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public M setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
		return (M)this;
	}
	
	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

	public M setBusinessStateType(Integer businessStateType) {
		set("business_state_type", businessStateType);
		return (M)this;
	}
	
	public Integer getBusinessStateType() {
		return getInt("business_state_type");
	}

	public M setAuthUserId(Integer authUserId) {
		set("auth_user_id", authUserId);
		return (M)this;
	}
	
	public Integer getAuthUserId() {
		return getInt("auth_user_id");
	}

	public M setAuthDeptId(Integer authDeptId) {
		set("auth_dept_id", authDeptId);
		return (M)this;
	}
	
	public Integer getAuthDeptId() {
		return getInt("auth_dept_id");
	}

	public M setOpened(Integer opened) {
		set("opened", opened);
		return (M)this;
	}
	
	public Integer getOpened() {
		return getInt("opened");
	}

}
