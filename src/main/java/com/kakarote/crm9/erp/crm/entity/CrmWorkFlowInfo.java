package com.kakarote.crm9.erp.crm.entity;

/**
 * CrmWorkFlowInfo class
 *
 * @author yue.li
 * @date 2019/12/20
 */
public class CrmWorkFlowInfo {

    private String type;

    private CrmWorkFlowData data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CrmWorkFlowData getData() {
        return data;
    }

    public void setData(CrmWorkFlowData data) {
        this.data = data;
    }
}
