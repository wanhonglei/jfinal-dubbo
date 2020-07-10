package com.kakarote.crm9.integration.entity;

import java.util.List;

/**
 * OaStaffs
 *
 * @author hao.fu
 * @create 2019/6/26 20:42
 */
public class OaStaffs {

    private String code;

    private List<OaStaff> data;

    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<OaStaff> getData() {
        return data;
    }

    public void setData(List<OaStaff> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
