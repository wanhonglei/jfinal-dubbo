package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: honglei.wan
 * @Description:商机变化枚举类
 * @Date: Create in 2020/4/20 5:43 下午
 */
public enum CrmBusinessChangeLogEnum {

    /**
     * 商机变化枚举类
     */
    OPEN_SEA(0,"公海"),
    BD(1,"BD"),
    DEPT(2,"部门"),
    ;

    private int code;
    private String msg;

    CrmBusinessChangeLogEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
