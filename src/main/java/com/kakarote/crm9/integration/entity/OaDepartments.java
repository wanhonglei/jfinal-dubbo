package com.kakarote.crm9.integration.entity;

import java.util.List;

/**
 * OaDepartments
 *
 * @author hao.fu
 * @create 2019/6/26 22:03
 */
public class OaDepartments {

    private String code;

    private List<OaDepartment> data;

    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<OaDepartment> getData() {
        return data;
    }

    public void setData(List<OaDepartment> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
