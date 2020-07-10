package com.kakarote.crm9.erp.crm.entity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Customer Revenue Statistic
 *
 * @author hao.fu
 * @create 2019/9/23 13:35
 */
public class CustomerRevenueStatistic {

    private BigDecimal totalRevenue;

    /**
     * Category and revenue map.
     * <p>
     * Map<CategoryId, CategoryRevenue>
     */
    private Map<Long, BigDecimal> categoryRevenueMap;

    /**
     * Map<CategoryCode, CategoryRevenue>
     */
    private Map<String, BigDecimal> categoryCodeRevenueMap;

    /**
     * Product and revenue map.
     * <p>
     * Map<CategoryId, Map<ProductId, ProductRevenue>>
     */
    private Map<Long, Map<Long, BigDecimal>> productRevenueMap;

    /**
     * Map<CategoryCode, Map<ProductName, ProductRevenue>>
     */
    private Map<String, Map<String, BigDecimal>> categoryCodeProductRevenueMap;

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<Long, BigDecimal> getCategoryRevenueMap() {
        return categoryRevenueMap;
    }

    public void setCategoryRevenueMap(Map<Long, BigDecimal> categoryRevenueMap) {
        this.categoryRevenueMap = categoryRevenueMap;
    }

    public Map<Long, Map<Long, BigDecimal>> getProductRevenueMap() {
        return productRevenueMap;
    }

    public void setProductRevenueMap(Map<Long, Map<Long, BigDecimal>> productRevenueMap) {
        this.productRevenueMap = productRevenueMap;
    }

    public Map<String, BigDecimal> getCategoryCodeRevenueMap() {
        return categoryCodeRevenueMap;
    }

    public void setCategoryCodeRevenueMap(Map<String, BigDecimal> categoryCodeRevenueMap) {
        this.categoryCodeRevenueMap = categoryCodeRevenueMap;
    }

    public Map<String, Map<String, BigDecimal>> getCategoryCodeProductRevenueMap() {
        return categoryCodeProductRevenueMap;
    }

    public void setCategoryCodeProductRevenueMap(Map<String, Map<String, BigDecimal>> categoryCodeProductRevenueMap) {
        this.categoryCodeProductRevenueMap = categoryCodeProductRevenueMap;
    }
}
