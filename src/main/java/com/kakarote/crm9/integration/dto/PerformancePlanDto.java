package com.kakarote.crm9.integration.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 履约侧回款计划模型
 *
 * @Author: haihong.wu
 * @Date: 2020/6/15 11:45 上午
 */
@Data
public class PerformancePlanDto implements Serializable {
    private static final long serialVersionUID = 1386485176551813782L;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 账单状态：to_pay-待支付；partial_pay-部分支付；paid-已支付；closed-已关闭
     */
    private String billStatus;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 累计已核销金额
     */
    private BigDecimal accumulateAmount;

    /**
     * 待核销金额
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
     * 预期还款完成时间
     */
    private Date expectPayTime;

    /**
     * 结束时间（成功or关闭）
     */
    private Date finishTime;

    /**
     * 最近还款时间
     */
    private Date lastPayTime;
}
