package com.kakarote.crm9.erp.crm.entity;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business Revenue Info
 *
 * @author hao.fu
 * @create 2019/9/19 16:48
 */
public class BusinessRevenueInfo {

    private Integer businessId;

    private BigDecimal totalRevenue;

    /**
     * Category and revenue map in receivable level.
     * Used for the report.
     * key is category, value is revenue.
     */
    private Map<Long, BigDecimal> categoryRevenueMap;

    /**
     * Map<CategoryId, Map<ProductId, ProductIncome>>
     */
    private Map<Long, Map<Long, BigDecimal>> productsIncomeByCategory;

    /**
     * Category and percentage map in business product level.
     * key is category, value is percentage.
     */
    private Map<Long, Double> categoryPercentageMap4BizLevel;

    /**
     * Products price statistic info
     */
    private BusinessProductPriceStatistic productPriceStatistic;

    /**
     * Business receivable statistic info
     */
    private BusinessReceivableInfoStatistic receivableInfoStatistic;

    /**
     * Category ID and code map
     */
    private Map<Long, String> catIdCodeMap;

    /**
     * Product ID and name map
     */
    private Map<Long, String> prodIdNameMap;

    /**
     * Default constructor
     */
    public BusinessRevenueInfo() {
        categoryPercentageMap4BizLevel = Maps.newHashMap();
        categoryRevenueMap = Maps.newHashMap();
        productsIncomeByCategory = Maps.newHashMap();
        catIdCodeMap = Maps.newHashMap();
        prodIdNameMap = Maps.newHashMap();
    }

    /**
     * Calculate each category revenue.
     */
    public void calculateRevenue() {
        if (totalRevenue == null || totalRevenue.intValue() == 0 || Objects.isNull(receivableInfoStatistic) || CollectionUtils.isEmpty(receivableInfoStatistic.getReceivableInfoList())) {
            return;
        }
        // Map<ReceivableId, List<BusinessReceivableInfo>> group by receivable id
        Map<Integer, List<BusinessReceivableInfo>> receivableGroupMap = receivableInfoStatistic.getReceivableInfoList().stream().collect(Collectors.groupingBy(BusinessReceivableInfo::getReceivableId));
        for(Integer item : receivableGroupMap.keySet()) {
            List<BusinessReceivableInfo> infos = receivableGroupMap.get(item);
            // only 1 receivable info means the receivable has only 1 product or does no refer to any product
            if (infos.size() == 1) {
                BusinessReceivableInfo info = infos.get(0);
                if (info.getIncome() == null || info.getIncome() == 0) {
                    continue;
                }
                if (Objects.nonNull(info.getProductId())) {
                    // update category revenue map
                    updateCategoryRevenueMapBySingleCatIncome(info.getCategoryId(), info.getIncome());
                    // update product revenue in specified category
                    updateProductsIncomeByCategoryMap(info.getCategoryId(), info.getProductId(), info.getIncome());
                } else {

                    // update category revenue map
                    Map<Long, Double> catRevenueMap = getIncomeMapForSpecifiedPercentageMap(info.getIncome(), categoryPercentageMap4BizLevel);
                    catRevenueMap.keySet().forEach(cat -> updateCategoryRevenueMapBySingleCatIncome(cat, catRevenueMap.get(cat)));

                    if (productPriceStatistic == null) {
                        return;
                    }
                    // update product revenue in specified category
                    Map<Long, Long> businessProdCategoryIdMap = productPriceStatistic.getProductPriceList().stream().collect(Collectors.toMap(BusinessProductPrice::getProductId, BusinessProductPrice::getCategoryId));
                    Map<Long, Double> prodIdPercentMap = productPriceStatistic.getPercentageMapForSpecifiedProducts(new ArrayList<>(businessProdCategoryIdMap.keySet()));
                    Map<Long, Double> prodRevenueMap = getIncomeMapForSpecifiedPercentageMap(info.getIncome(), prodIdPercentMap);
                    prodRevenueMap.keySet().forEach(prodId -> updateProductsIncomeByCategoryMap(businessProdCategoryIdMap.get(prodId), prodId, prodRevenueMap.get(prodId)));
                }
            // more than 1 record means the receivable refers to multiple products which may belongs to multiple category
            } else {
                Double totalIncome = infos.get(0).getIncome();
                if (totalIncome == null || totalIncome == 0) {
                    continue;
                }

                Map<Long, Long> prodCatIdMap = infos.stream().collect(Collectors.toMap(BusinessReceivableInfo::getProductId, BusinessReceivableInfo::getCategoryId));
                Map<Long, Double> prodPercentageMap = productPriceStatistic.getPercentageMapForSpecifiedProducts(new ArrayList<>(prodCatIdMap.keySet()));
                Map<Long, Double> prodIncomeMap = getIncomeMapForSpecifiedPercentageMap(totalIncome, prodPercentageMap);

                // update category revenue map
                Map<Long, Double> catIncomeMap = Maps.newHashMap();
                prodIncomeMap.keySet().forEach(prodId -> {
                    Long catId = prodCatIdMap.get(prodId);
                    if (Objects.isNull(catIncomeMap.get(prodId))) {
                        catIncomeMap.put(catId, prodIncomeMap.get(prodId));
                    } else {
                        catIncomeMap.put(catId, prodIncomeMap.get(prodId) + catIncomeMap.get(catId));
                    }
                });
                catIncomeMap.keySet().forEach(cat -> updateCategoryRevenueMapBySingleCatIncome(cat, catIncomeMap.get(cat)));

                // update product revenue in specified category
                prodIncomeMap.keySet().forEach(prodId -> updateProductsIncomeByCategoryMap(prodCatIdMap.get(prodId), prodId, prodIncomeMap.get(prodId)));
            }
        }

        categoryRevenueMap = adjustResult(categoryRevenueMap, totalRevenue);
        productsIncomeByCategory = adjustPorductIncomeByCategoryResult();
    }

    /**
     * Adjust revenue: round half up.
     */
    private Map<Long, BigDecimal> adjustResult(Map<Long, BigDecimal> targetMap, BigDecimal totalRevenue) {
        if (targetMap.size() < 1) {
            return targetMap;
        }
        Map<Long, BigDecimal> result = Maps.newHashMap();
        List<Long> ids = new ArrayList<>(targetMap.keySet());
        for (int index = 0; index < ids.size(); index++) {
            if (index == ids.size() - 1) {
                BigDecimal others = result.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                result.put(ids.get(index), totalRevenue.subtract(others));
            } else {
                result.put(ids.get(index), targetMap.get(ids.get(index)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        }
        return result;
    }

    /**
     * Adjust product income result.
     */
    private Map<Long, Map<Long, BigDecimal>> adjustPorductIncomeByCategoryResult() {
        if (productsIncomeByCategory.size() < 1) {
            return productsIncomeByCategory;
        }
        Map<Long, Map<Long, BigDecimal>> result = Maps.newHashMap();
        productsIncomeByCategory.keySet().forEach(catId -> result.put(catId, adjustResult(productsIncomeByCategory.get(catId), categoryRevenueMap.get(catId))));
        return result;
    }

    /**
     * Update category revenue map with single category income.
     *
     * @param catId category id
     * @param singleIncome single category income
     */
    private void updateCategoryRevenueMapBySingleCatIncome(Long catId, Double singleIncome) {
        BigDecimal oldValue = getDecimalValueFromMap(categoryRevenueMap, catId, new BigDecimal(singleIncome));
        categoryRevenueMap.put(catId, oldValue.add(new BigDecimal(singleIncome)));
    }

    /**
     * Update category/products income map
     *
     * @param catId category id
     * @param prodId product id
     * @param income product income
     */
    private void updateProductsIncomeByCategoryMap(Long catId, Long prodId, Double income) {
        Map<Long, BigDecimal> newProd = Maps.newHashMap();
        newProd.put(prodId, new BigDecimal(income));

        Map<Long, BigDecimal> existingProdsMap = productsIncomeByCategory.putIfAbsent(catId, newProd);
        existingProdsMap = Objects.isNull(existingProdsMap) ? Maps.newHashMap() : existingProdsMap;

        BigDecimal existingProdMoney = getDecimalValueFromMap(existingProdsMap, prodId, new BigDecimal(income));
        existingProdsMap.put(prodId, existingProdMoney.add(new BigDecimal(income)));
        productsIncomeByCategory.put(catId, existingProdsMap);
    }

    private BigDecimal getDecimalValueFromMap(Map<Long, BigDecimal> map, Long key, BigDecimal value) {
        BigDecimal existingValue = map.putIfAbsent(key, value);
        return Objects.isNull(existingValue) ? new BigDecimal(0) : existingValue;
    }

    /**
     * Get id/income map for specified percentage id map.
     *
     * @param totalIncome total income
     * @param percentageMap maybe categoryId/income map or productId/income map
     * @return id/income map
     */
    private Map<Long, Double> getIncomeMapForSpecifiedPercentageMap(Double totalIncome, Map<Long, Double> percentageMap) {
        Map<Long, Double> result = Maps.newHashMap();
        ArrayList<Long> ids = new ArrayList<>(percentageMap.keySet());
        for(int index = 0; index < ids.size(); index++) {
            if (index == ids.size() - 1) {
                Double others = result.values().stream().reduce(Double::sum).orElse(0d);
                result.put(ids.get(index), totalIncome - others);
            } else {
                result.put(ids.get(index), totalIncome * percentageMap.get(ids.get(index)));
            }
        }
        System.out.println("totalIncome:" + totalIncome);
        result.keySet().forEach(item -> System.out.println(item + ":" + result.get(item)));
        return result;
    }

    /**
     * Return category revenue map for display.
     * Map<CategoryCode, CategoryRevenue>
     *
     * @return category revenue map for display
     */
    public Map<String, BigDecimal> getCategoryRevenueMapForDisplay() {
        Map<String, BigDecimal> result = Maps.newHashMap();
        if (Objects.nonNull(catIdCodeMap) && Objects.nonNull(categoryRevenueMap)) {
            categoryRevenueMap.keySet().forEach(item -> {
                result.put(catIdCodeMap.get(item), categoryRevenueMap.get(item));
            });
        }
        return result;
    }

    /**
     * Return product revenue map based on category for display.
     * Map<CategoryName, Map<ProductName, ProductRevenue>>
     *
     * @return product revenue map based on category for display
     */
    public Map<String, Map<String, BigDecimal>> getProductsIncomeByCategoryForDisplay() {
        Map<String, Map<String, BigDecimal>> result = Maps.newHashMap();

        if (Objects.nonNull(prodIdNameMap) && Objects.nonNull(productsIncomeByCategory)) {
            productsIncomeByCategory.keySet().forEach(catId -> {
                Map<String, BigDecimal> prodsMap = Maps.newHashMap();
                productsIncomeByCategory.get(catId).keySet().forEach(prodKey -> {
                    prodsMap.put(prodIdNameMap.get(prodKey), productsIncomeByCategory.get(catId).get(prodKey));
                });
                result.put(catIdCodeMap.get(catId), prodsMap);
            });
        }
        return result;
    }

    public Map<Long, BigDecimal> getCategoryRevenueMap() {
        return categoryRevenueMap;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<Long, Double> getCategoryPercentageMap4BizLevel() {
        return categoryPercentageMap4BizLevel;
    }

    public void setCategoryPercentageMap4BizLevel(Map<Long, Double> categoryPercentageMap4BizLevel) {
        this.categoryPercentageMap4BizLevel = categoryPercentageMap4BizLevel;
    }

    public void setCategoryRevenueMap(Map<Long, BigDecimal> categoryRevenueMap) {
        this.categoryRevenueMap = categoryRevenueMap;
    }

    public BusinessReceivableInfoStatistic getReceivableInfoStatistic() {
        return receivableInfoStatistic;
    }

    public void setReceivableInfoStatistic(BusinessReceivableInfoStatistic receivableInfoStatistic) {
        this.receivableInfoStatistic = receivableInfoStatistic;
    }

    public BusinessProductPriceStatistic getProductPriceStatistic() {
        return productPriceStatistic;
    }

    public void setProductPriceStatistic(BusinessProductPriceStatistic productPriceStatistic) {
        this.productPriceStatistic = productPriceStatistic;
    }

    public Map<Long, Map<Long, BigDecimal>> getProductsIncomeByCategory() {
        return productsIncomeByCategory;
    }

    public void setProductsIncomeByCategory(Map<Long, Map<Long, BigDecimal>> productsIncomeByCategory) {
        this.productsIncomeByCategory = productsIncomeByCategory;
    }

    public Map<Long, String> getCatIdCodeMap() {
        return catIdCodeMap;
    }

    public void setCatIdCodeMap(Map<Long, String> catIdCodeMap) {
        this.catIdCodeMap = catIdCodeMap;
    }

    public Map<Long, String> getProdIdNameMap() {
        return prodIdNameMap;
    }

    public void setProdIdNameMap(Map<Long, String> prodIdNameMap) {
        this.prodIdNameMap = prodIdNameMap;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
