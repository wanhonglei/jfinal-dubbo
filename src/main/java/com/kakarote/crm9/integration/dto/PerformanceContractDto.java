package com.kakarote.crm9.integration.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 履约侧合同模型
 * @Author: haihong.wu
 * @Date: 2020/6/15 11:41 上午
 */
@Data
public class PerformanceContractDto implements Serializable {
    private static final long serialVersionUID = -2353583841462724106L;

    private Integer userId;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 合同金额
     */
    private BigDecimal contractAmount;

    /**
     * 合同申请审批进度
     */
    private String checkStatus;

    /**
     * 币种
     */
    private String currencyType;

    /**
     * 最新回款时间
     */
    private Date latelyPaymentTime;

    /**
     * 回款计划
     */
    private List<PerformancePlanDto> plans;
}
