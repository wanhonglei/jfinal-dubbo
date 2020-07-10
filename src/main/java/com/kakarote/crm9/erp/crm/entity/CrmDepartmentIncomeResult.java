package com.kakarote.crm9.erp.crm.entity;

import java.math.BigDecimal;
import java.util.Map;

public class CrmDepartmentIncomeResult {

    /**当期收入*/
    private Map<String, BigDecimal> currentIncome;

    /**月预测*/
    private Map<String,BigDecimal> monthForecast;

    /**按省份统计商品明细*/
    private Map<String,Map<String,BigDecimal>> provinceProductIncome;

    public Map<String, BigDecimal> getCurrentIncome() {
        return currentIncome;
    }

    public void setCurrentIncome(Map<String, BigDecimal> currentIncome) {
        this.currentIncome = currentIncome;
    }

    public Map<String, BigDecimal> getMonthForecast() {
        return monthForecast;
    }

    public void setMonthForecast(Map<String, BigDecimal> monthForecast) {
        this.monthForecast = monthForecast;
    }

    public Map<String, Map<String, BigDecimal>> getProvinceProductIncome() {
        return provinceProductIncome;
    }

    public void setProvinceProductIncome(Map<String, Map<String, BigDecimal>> provinceProductIncome) {
        this.provinceProductIncome = provinceProductIncome;
    }
}
