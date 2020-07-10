package com.kakarote.crm9.erp.crm.entity;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Business Category Price Statistic
 *
 * @author hao.fu
 * @since 2019/9/18 19:40
 */
public class BusinessCategoryPriceStatistic {

    private Long businessId;
    private List<BusinessCategoryPrice> categoryPriceList;

    /**
     * key is category id, value is price percentage
     */
    private Map<Long, Double> categoryPricePercentageMap;

    /**
     * Default constructor
     */
    public BusinessCategoryPriceStatistic() {
        categoryPriceList = Lists.newArrayList();
        categoryPricePercentageMap = Maps.newHashMap();
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Map<Long, Double> getCategoryPricePercentageMap() {
        return categoryPricePercentageMap;
    }

    public void setCategoryPricePercentageMap(Map<Long, Double> categoryPricePercentageMap) {
        this.categoryPricePercentageMap = categoryPricePercentageMap;
    }

    public List<BusinessCategoryPrice> getCategoryPriceList() {
        return categoryPriceList;
    }

    public void setCategoryPriceList(List<BusinessCategoryPrice> categoryPriceList) {
        this.categoryPriceList = categoryPriceList;
    }

    public void calculatePercentage() {
        if (CollectionUtils.isNotEmpty(categoryPriceList)) {
            DecimalFormat df = new DecimalFormat("#.000000");
            Double totalPrice = categoryPriceList.stream().mapToDouble(BusinessCategoryPrice::getTotalPrice).sum();
            for (int index = 0; index < categoryPriceList.size(); index++) {
                BusinessCategoryPrice item = categoryPriceList.get(index);
                if (index == categoryPriceList.size() - 1) {
                    Double otherPercentage = categoryPricePercentageMap.values().stream().reduce(Double::sum).orElse(0d);
                    categoryPricePercentageMap.put(item.getCategoryId(), 1 - otherPercentage);
                } else {
                    if(totalPrice >0) {
                        categoryPricePercentageMap.put(item.getCategoryId(), Double.valueOf(df.format(item.getTotalPrice() / totalPrice)));
                    }else{
                        categoryPricePercentageMap.put(item.getCategoryId(), Double.valueOf(NumberUtils.INTEGER_ZERO));
                    }
                }
            }
            System.out.println("total percentage:" + categoryPricePercentageMap.values().stream().reduce(Double::sum));
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
