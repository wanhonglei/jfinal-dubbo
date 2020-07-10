package com.kakarote.crm9.erp.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liming.guo
 */
@Data
@Builder
public class CrmBusinessStatusDto {

    /**
     * 商机组ID
     */
    private Long groupId;

    private Long statusId;
    /**
     * 商机阶段名称
     */
    private String statusName;
    /**
     * 可得性 60
     */
    private BigDecimal rate;
    /**
     * 启用状态 1 开启 2 封存
     */
    private Integer opened;

    private Integer orderNum;
    /**
     * 活动名称结合
     */
    private String activityNames;
    /**
     * 可验证结果名称集合
     */
    private String verificationNames;
    /**
     * 创建人
     */
    private Long createUserId;
    /**
     * 关键销售活动
     */
    private List<CrmBusinessStatusSalesActivityDto> activityList;
    /**
     * 可验证结果
     */
    private List<CrmBusinessStatusVerificationDto> verificationList;


}
