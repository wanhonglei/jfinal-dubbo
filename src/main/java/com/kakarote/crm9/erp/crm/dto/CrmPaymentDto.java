package com.kakarote.crm9.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crm Payment Dto
 *
 * @author yue.li
 * @since 2019/11/19 19:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmPaymentDto {

    /**模糊搜索条件*/
    private String search;

    /**支付开始日期*/
    private String payStartTime;

    /**支付结束日期*/
    private String payEndTime;

    /**状态*/
    private String state;

    /**场景ID*/
    private String sceneId;
}
