package com.kakarote.crm9.integration.entity;

import com.google.common.collect.Lists;
import com.jfinal.kit.JsonKit;

import java.io.Serializable;
import java.util.List;

/**
 * Update Payment Request
 *
 * @author hao.fu
 * @create 2019/11/12 20:25
 */
public class UpdatePaymentRequest implements Serializable {

    private static final long serialVersionUID = 2265844464883201538L;

    List<String> paymentNoList;

    public UpdatePaymentRequest() {
        paymentNoList = Lists.newArrayList();
    }

    public List<String> getPaymentNoList() {
        return paymentNoList;
    }

    public void setPaymentNoList(List<String> paymentNoList) {
        this.paymentNoList = paymentNoList;
    }

    @Override
    public String toString() {
        return JsonKit.toJson(this);
    }
}
