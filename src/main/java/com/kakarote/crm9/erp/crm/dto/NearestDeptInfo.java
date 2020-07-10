package com.kakarote.crm9.erp.crm.dto;

import lombok.Data;

/**
 * @Author: honglei.wan
 * @Description:最近部门的信息
 * @Date: Create in 2020/4/22 10:18 上午
 */
@Data
public class NearestDeptInfo {

    /**
     * 部门id
     */
    private Integer deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门周转库库容
     */
    private Integer capacity;

    /**
     * 已使用库容
     */
    private Integer usedCapacity;

}
