package com.kakarote.crm9.erp.admin.entity.base;

import com.jfinal.plugin.activerecord.CrmModel;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 * @author liming.guo
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseCrmBusinessGroup<M extends BaseCrmBusinessGroup<M>> extends CrmModel<M> implements IBean {

	public M setId(Long id) {
		set("id", id);
		return (M)this;
	}
	
	public Long getId() {
		return getLong("id");
	}

	public M setName(String name) {
		set("name", name);
		return (M)this;
	}
	
	public String getName() {
		return getStr("name");
	}

	public M setDeptIds(Long deptIds) {
		set("dept_ids", deptIds);
		return (M)this;
	}
	
	public Long getDeptIds() {
		return getLong("dept_ids");
	}

	public M setCreateUserId(Long createUserId) {
		set("create_user_id", createUserId);
		return (M)this;
	}
	
	public Long getCreateUserId() {
		return getLong("create_user_id");
	}

	public M setUpdateUserId(Long updateUserId) {
		set("update_user_id", updateUserId);
		return (M)this;
	}

	public Long getUpdateUserId() {
		return getLong("update_user_id");
	}

	public M setDeptEmailGroup(String deptEmailGroup) {
		set("dept_email_group", deptEmailGroup);
		return (M)this;
	}
	
	public String getDeptEmailGroup() {
		return getStr("dept_email_group");
	}

	public M setStatus(Integer status) {
		set("status", status);
		return (M)this;
	}
	
	public Integer getStatus() {
		return getInt("status");
	}

	public M setIsDeleted(Integer isDeleted) {
		set("is_deleted", isDeleted);
		return (M)this;
	}
	
	public Integer getIsDeleted() {
		return getInt("is_deleted");
	}

	public M setGmtCreate(java.util.Date gmtCreate) {
		set("gmt_create", gmtCreate);
		return (M)this;
	}
	
	public java.util.Date getGmtCreate() {
		return get("gmt_create");
	}

	public M setGmtModified(java.util.Date gmtModified) {
		set("gmt_modified", gmtModified);
		return (M)this;
	}
	
	public java.util.Date getGmtModified() {
		return get("gmt_modified");
	}

	public M setEnvFlag(String envFlag) {
		set("env_flag", envFlag);
		return (M)this;
	}
	
	public String getEnvFlag() {
		return getStr("env_flag");
	}

	public M setRemark(String remark) {
		set("remark", remark);
		return (M)this;
	}
	
	public String getRemark() {
		return getStr("remark");
	}

}
