package com.kakarote.crm9.erp.admin.entity;

import com.kakarote.crm9.erp.admin.entity.base.BaseAdminRecord;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class AdminRecord extends BaseAdminRecord<AdminRecord> {
	public static final AdminRecord dao = new AdminRecord().dao();
	//是否添加至日程提醒（0.否，1.是）
	private Integer isEvent;
	
	//通话记录id（多个id使用","分割）
	private String callRecordIdList;

	public Integer getIsEvent() {
		return isEvent;
	}

	public void setIsEvent(Integer isEvent) {
		this.isEvent = isEvent;
	}

	public String getCallRecordIdList() {
		return callRecordIdList;
	}

	public void setCallRecordIdList(String callRecordIdList) {
		this.callRecordIdList = callRecordIdList;
	}

	public AdminRecord build(){
		AdminRecord record=new AdminRecord();
		return record._setAttrs(this);
	}
}
