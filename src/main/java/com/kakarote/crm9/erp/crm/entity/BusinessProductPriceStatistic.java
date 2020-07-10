package com.kakarote.crm9.erp.crm.entity;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business Product Price Statistic
 *
 * @author hao.fu
 * @since 2019/9/19 13:40
 */
public class BusinessProductPriceStatistic {

    private Long businessId;
    private List<BusinessProductPrice> productPriceList;

    /**
     * Default constructor
     */
    public BusinessProductPriceStatistic() {
        productPriceList = Lists.newArrayList();
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public List<BusinessProductPrice> getProductPriceList() {
        return productPriceList;
    }

    public void setProductPriceList(List<BusinessProductPrice> productPriceList) {
        this.productPriceList = productPriceList;
    }

    /**
     * Get product price percentage map for specified products.
     *
     * @param productsIds product ids
     * @return product price percentage map, key is product id, value is percentage
     */
    public Map<Long, Double> getPercentageMapForSpecifiedProducts(List<Long> productsIds) {
        if (CollectionUtils.isEmpty(productsIds) || CollectionUtils.isEmpty(productPriceList)) {
            return Collections.EMPTY_MAP;
        }
        List<Long> bizProdIds = productPriceList.stream().map(BusinessProductPrice::getProductId).collect(Collectors.toList());
        if (bizProdIds.containsAll(productsIds)) {
            return calculateProductPricePercentage(productsIds);
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    private Map<Long, Double> calculateProductPricePercentage(List<Long> productsIds) {
        Map<Long, Double> result = Maps.newHashMap();
        List<BusinessProductPrice> prodsList = productPriceList.stream().filter(item -> productsIds.contains(item.getProductId())).collect(Collectors.toList());
        Double totalPrice = prodsList.stream().mapToDouble(BusinessProductPrice::getSalesPrice).sum();
        DecimalFormat df = new DecimalFormat("#.000000");
        for (int index = 0; index < prodsList.size(); index++) {
            BusinessProductPrice item = prodsList.get(index);
            if (index == prodsList.size() - 1) {
                Double otherPercentage = result.values().stream().reduce(Double::sum).orElse(0d);
                result.put(item.getProductId(), 1 - otherPercentage);
            } else {
                if(BigDecimal.ZERO.compareTo(new BigDecimal(totalPrice)) == 0) {
                    result.put(item.getProductId(), NumberUtils.DOUBLE_ZERO);
                }else{
                    result.put(item.getProductId(), Double.valueOf(df.format(item.getSalesPrice() / totalPrice)));
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
