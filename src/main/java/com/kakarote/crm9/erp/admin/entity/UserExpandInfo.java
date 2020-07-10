package com.kakarote.crm9.erp.admin.entity;

import java.io.Serializable;
import java.util.List;
/**
 * UserExpandInfo.
 *
 * @author yue.li
 * @create 2019/11/27 10:00
 */
public class UserExpandInfo implements Serializable {

    /**
     * 事业部ID
     */
    private int businessDepartmentId;

    /**
     * 事业部名称
     */
    private String businessDepartmentName;

    /**
     * 角色名称
     */
    private List<String> roleNames;

    public int getBusinessDepartmentId() {
        return businessDepartmentId;
    }

    public void setBusinessDepartmentId(int businessDepartmentId) {
        this.businessDepartmentId = businessDepartmentId;
    }

    public String getBusinessDepartmentName() {
        return businessDepartmentName;
    }

    public void setBusinessDepartmentName(String businessDepartmentName) {
        this.businessDepartmentName = businessDepartmentName;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }
}
