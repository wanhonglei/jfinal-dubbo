package com.kakarote.crm9.erp.crm.entity;

import java.util.List;

public class CrmServiceTag {

    /**业务ID*/
    private String id;
    /**标签集合*/
    private List<CrmBaseTag> tag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CrmBaseTag> getTag() {
        return tag;
    }

    public void setTag(List<CrmBaseTag> tag) {
        this.tag = tag;
    }
}
