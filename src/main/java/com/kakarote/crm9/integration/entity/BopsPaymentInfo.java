package com.kakarote.crm9.integration.entity;

import java.util.List;

/**
 * BOPS Payment Response
 *
 * @author hao.fu
 * @create 2019/10/14 16:26
 */
public class BopsPaymentInfo {

    private String code;

    private List<PaymentDetail> data;

    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<PaymentDetail> getData() {
        return data;
    }

    public void setData(List<PaymentDetail> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
