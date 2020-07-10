package com.kakarote.crm9.erp.crm.entity;

import com.alibaba.fastjson.JSON;

/**
 * Business Category Price
 *
 * @author hao.fu
 * @since 2019/9/18 19:35
 */
public class BusinessCategoryPrice {

    private Integer businessId;
    private Long categoryId;
    private String categoryName;
    private Double totalPrice;
    private String categoryCode;

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
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

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
