package com.kakarote.crm9.erp.admin.entity.base;

import com.jfinal.plugin.activerecord.CrmModel;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseAdminRecord<M extends BaseAdminRecord<M>> extends CrmModel<M> implements IBean {

	public M setRecordId(Long recordId) {
		set("record_id", recordId);
		return (M)this;
	}

	public Long getRecordId() {
		return getLong("record_id");
	}

	public M setTypes(String types) {
		set("types", types);
		return (M)this;
	}

	public String getTypes() {
		return getStr("types");
	}

	public M setTypesId(Integer typesId) {
		set("types_id", typesId);
		return (M)this;
	}

	public Integer getTypesId() {
		return getInt("types_id");
	}

	public M setContent(String content) {
		set("content", content);
		return (M)this;
	}

	public String getContent() {
		return getStr("content");
	}

	public M setCategory(String category) {
		set("category", category);
		return (M)this;
	}

	public String getCategory() {
		return getStr("category");
	}

	public M setNextTime(java.util.Date nextTime) {
		set("next_time", nextTime);
		return (M)this;
	}

	public java.util.Date getNextTime() {
		return get("next_time");
	}

	public M setBusinessIds(String businessIds) {
		set("business_ids", businessIds);
		return (M)this;
	}

	public String getBusinessIds() {
		return getStr("business_ids");
	}

	public M setContactsIds(String contactsIds) {
		set("contacts_ids", contactsIds);
		return (M)this;
	}

	public String getContactsIds() {
		return getStr("contacts_ids");
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

	public M setCreateUserId(Integer createUserId) {
		set("create_user_id", createUserId);
		return (M)this;
	}

	public Integer getCreateUserId() {
		return getInt("create_user_id");
	}

	public M setBatchId(String batchId) {
		set("batch_id", batchId);
		return (M)this;
	}

	public String getBatchId() {
		return getStr("batch_id");
	}

	//通知人
	public M setSendUserIds(String sendUserIds) {
		set("send_user_ids", sendUserIds);
		return (M)this;
	}

	public String getSendUserIds() {
		return getStr("send_user_ids");
	}

	//客户
	public M setCustomerIds(String customerIds) {
		set("customer_ids", customerIds);
		return (M)this;
	}

	public String getCustomerIds() {
		return getStr("customer_ids");
	}

	//线索
	public M setLeadsIds(String leadsIds) {
		set("leads_ids", leadsIds);
		return (M)this;
	}

	public String getLeadsIds() {
		return getStr("leads_ids");
	}

	public M setChannel(Integer channel) {
		set("channel", channel);
		return (M)this;
	}

	public Integer getChannel() {
		return getInt("channel");
	}

	public M setBusinessStageId(java.lang.Integer businessStageId) {
		set("business_stage_id", businessStageId);
		return (M)this;
	}

	public java.lang.Integer getBusinessStageId() {
		return getInt("business_stage_id");
	}

	public M setIsEnd(java.lang.Integer isEnd) {
		set("is_end", isEnd);
		return (M)this;
	}

	public java.lang.Integer getIsEnd() {
		return getInt("is_end");
	}
}
