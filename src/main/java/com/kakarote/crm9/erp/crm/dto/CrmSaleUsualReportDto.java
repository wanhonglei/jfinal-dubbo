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
public class CrmSaleUsualReportDto {
    /**部门ID*/
    private Integer deptId;

    /**开始时间*/
    private String startTime;

    /**结束时间*/
    private String endTime;

    /**BD名称*/
    private String saleUserName;
    /**BD*/
    private String saleUserId;
}
