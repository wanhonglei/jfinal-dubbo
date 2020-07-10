package com.kakarote.crm9.integration.entity;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;

/**
 * Payment Detail From BOPS
 *
 * @author hao.fu
 * @create 2019/10/11 14:56
 */
public class PaymentDetail {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 客户名称
     */
    private String payName;

    /**
     * 总支付金额
     */
    private BigDecimal totalAmount;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 支付的对方账户帐号
     */
    private String parternAccount;

    /**
     * 收款账号
     */
    private String seller;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付流水号
     */
    private String outerPaymentNo;

    /**
     * 支付号
     */
    private String paymentNo;

    /**
     * 支付时间
     */
    private String payTime;

    /**
     * 备注信息
     */
    private String comment;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getParternAccount() {
        return parternAccount;
    }

    public void setParternAccount(String parternAccount) {
        this.parternAccount = parternAccount;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOuterPaymentNo() {
        return outerPaymentNo;
    }

    public void setOuterPaymentNo(String outerPaymentNo) {
        this.outerPaymentNo = outerPaymentNo;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}