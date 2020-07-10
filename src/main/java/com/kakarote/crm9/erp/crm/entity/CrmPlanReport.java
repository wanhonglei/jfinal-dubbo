package com.kakarote.crm9.erp.crm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CrmCustomerNoteRequest class
 *
 * @author yue.li
 * @date 2020/04/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmPlanReport {

    /**
     * 商机阶段
     */
    public String businessTypeId;

    /**
     * DB
     */
    public String businessUserId;

    /**
     * 预计成交开始日期
     */
    public String dealStartDate;

    /**
     * 预计成交结束日期
     */
    public String dealEndDate;

    /**
     * 业务部门
     */
    public String businessDeptId;

    /**
     * 创建开始时间
     */
    public String createStartTime;

    /**
     * 创建结束时间
     */
    public String createEndTime;

    /**
     * 省份
     */
    public String mapAddress;

    /**
     * 事业部ID
     */
    private Long deptId;

    /**
     * 商机组ID
     */
    private Long groupId;

    /**
     * 商机阶段ID
     */
    private String statusId;

}
