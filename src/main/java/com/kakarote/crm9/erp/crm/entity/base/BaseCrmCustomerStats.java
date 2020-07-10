package com.kakarote.crm9.erp.crm.entity.base;

import com.jfinal.plugin.activerecord.CrmModel;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseCrmCustomerStats<M extends BaseCrmCustomerStats<M>> extends CrmModel<M> implements IBean {

	public M setId(Long id) {
		set("id", id);
		return (M)this;
	}

	public Long getId() {
		return getLong("id");
	}

	public M setUserId(Integer userId) {
		set("user_id", userId);
		return (M)this;
	}

	public Integer getUserId() {
		return getInt("user_id");
	}

	public M setCustomerNum(Integer customerNum) {
		set("customer_num", customerNum);
		return (M)this;
	}

	public Integer getCustomerNum() {
		return getInt("customer_num");
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

}
