package com.kakarote.crm9.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crm Department Income Report Dto
 *
 * @author yue.li
 * @since 2019/11/19 19:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmDepartmentIncomeReportDto {

    /**部门ID*/
    private Integer deptId;

    /**分析开始时间*/
    private String startTime;

    /**分析结束时间*/
    private String endTime;
}
