package com.kakarote.crm9.integration.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 履约侧核销数据模型
 * @Author: haihong.wu
 * @Date: 2020/6/15 3:48 下午
 */
@Data
public class PerformanceVerificationDto implements Serializable {
    private static final long serialVersionUID = 7142164196675510439L;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 核销号
     */
    private String verificationNo;

    /**
     * 核销单状态：to_pay-待支付；paid-已支付；success-成功；closed-已关闭
     */
    private String verificationStatus;

    /**
     * 本次核销金额
     */
    private BigDecimal verificationAmount;

    /**
     * 本次核销后的累计已核销金额
     */
    private BigDecimal accumulateAmount;

    /**
     * 本次核销后的待核销金额
     */
    private BigDecimal waitAmount;

    /**
     * 合同号
     */
    private String parentBizNo;

    /**
     * 合同回款计划号
     */
    private String bizNo;

    /**
     * 业务类型：contract_fulfill-合同
     */
    private String bizType;

    /**
     * 内部支付号
     */
    private String innerPaymentNo;

    /**
     * 外部交易流水号
     */
    private String outerTradeNo;

    /**
     * 结束时间（成功or关闭）
     */
    private Date finishTime;
}
