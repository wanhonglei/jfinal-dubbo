package com.kakarote.crm9.erp.crm.entity;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Business Receivable Info Statistic
 *
 * @author hao.fu
 * @since 2019/9/19 16:20
 */
public class BusinessReceivableInfoStatistic {

    private Long businessId;

    private List<BusinessReceivableInfo> receivableInfoList;

    /**
     * Default constructor
     */
    public BusinessReceivableInfoStatistic() {
        receivableInfoList = Lists.newArrayList();
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public List<BusinessReceivableInfo> getReceivableInfoList() {
        return receivableInfoList;
    }

    public void setReceivableInfoList(List<BusinessReceivableInfo> receivableInfoList) {
        this.receivableInfoList = receivableInfoList;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
