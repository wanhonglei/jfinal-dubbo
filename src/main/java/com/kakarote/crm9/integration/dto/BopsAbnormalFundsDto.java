package com.kakarote.crm9.integration.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/17 4:43 下午
 */
@Data
public class BopsAbnormalFundsDto implements Serializable {
    private static final long serialVersionUID = 3587496482488254448L;

    /**
     * 人工干预状态
     */
    private String interventionStatus;

    /**
     * 支付号 qx_payment.payment_no
     */
    private String paymentNo;

    /**
     * oa 审批号
     */
    private String oaNo;
}
