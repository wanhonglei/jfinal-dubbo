package com.kakarote.crm9.erp.crm.entity;

import com.alibaba.fastjson.JSON;

/**
 * Product price for business
 *
 * @author hao.fu
 * @since 2019/9/19 11:54
 */
public class BusinessProductPrice {

    private Integer businessId;
    private Long productId;
    private String productName;
    private Double salesPrice;
    private Long categoryId;

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
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

    public Double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(Double salesPrice) {
        this.salesPrice = salesPrice;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
