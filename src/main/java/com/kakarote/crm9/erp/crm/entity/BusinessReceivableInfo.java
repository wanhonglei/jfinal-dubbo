package com.kakarote.crm9.erp.crm.entity;

import com.alibaba.fastjson.JSON;

/**
 * Business Receivable Info
 *
 * @author hao.fu
 * @since 2019/9/19 15:47
 */
public class BusinessReceivableInfo {

    private Integer businessId;
    private Integer receivableId;
    private Double income;
    private Long categoryId;
    private String categoryName;
    private Long productId;
    private String productName;

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public Integer getReceivableId() {
        return receivableId;
    }

    public void setReceivableId(Integer receivableId) {
        this.receivableId = receivableId;
    }

    public Double getIncome() {
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
