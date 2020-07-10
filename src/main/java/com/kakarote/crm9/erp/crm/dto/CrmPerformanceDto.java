package com.kakarote.crm9.erp.crm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/4/15 3:05 下午
 */
@Data
public class CrmPerformanceDto implements Serializable {
    /**
     * 网站业绩
     */
    private BigDecimal websitePerformance;
    /**
     * BD业绩
     */
    private BigDecimal bdPerformance;
    /**
     * bops侧到单金额
     */
    private BigDecimal orderAmount;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 客户ID
     */
    private Long customerId;
    /**
     * 申请时间
     */
    private Date receiveTime;
}
