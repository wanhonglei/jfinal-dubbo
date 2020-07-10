package com.kakarote.crm9.erp.crm.entity;

/**
 * 封装商品展示类
 * @author yue.li
 * @date 2019/11/19
 */
public class CrmProductDetailDto extends CrmProductDetail {

    /**批次ID*/
    private String batchId;

    /**商品大类编码*/
    private String categoryCode;

    /**商品大类ID*/
    private Integer categoryId;

    /**用户ID*/
    private Integer userId;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
