package com.kakarote.crm9.integration.entity;

/**
 * OaDepartment
 *
 * @author hao.fu
 * @create 2019/6/26 22:03
 */
public class OaDepartment {

    private String deptId;

    private String deptLevel;

    private String name;

    private String pid;

    private String leaderId;

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDeptLevel() {
        return deptLevel;
    }

    public void setDeptLevel(String deptLevel) {
        this.deptLevel = deptLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }
}
