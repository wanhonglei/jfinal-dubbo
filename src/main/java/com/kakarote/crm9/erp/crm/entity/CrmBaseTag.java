package com.kakarote.crm9.erp.crm.entity;

public class CrmBaseTag {

    /**标签ID*/
    private String id;
    /**标签名称*/
    private String name;
    /**是否共有*/
    private boolean isPublic;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
