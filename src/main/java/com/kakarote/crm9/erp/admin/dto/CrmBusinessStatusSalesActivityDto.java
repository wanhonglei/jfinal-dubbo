package com.kakarote.crm9.erp.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 关键活动
 *
 * @author liming.guo
 */
@Data
@Builder
public class CrmBusinessStatusSalesActivityDto {

    /**
     * 关键活动编号
     */
    private Long activityId;
    /**
     * 活动名称
     */
    private String activityName;

}
