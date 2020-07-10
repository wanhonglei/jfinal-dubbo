package com.kakarote.crm9.erp.crm.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 商机报表查询参数对象
 * @author honglei.wan
 */
@Setter
@Getter
public class CrmBusinessReport {

    /**
     * 商机名称
     */
    public String businessName;

    /**
     * 客户名称
     */
    public String customerName;

    /**
     * 部门ID
     */
    public Integer deptId;

    /**
     * 创建开始时间
     * 格式：YYYY-MM-DD
     */
    public String createStartTime;

    /**
     * 创建结束时间
     * 格式：YYYY-MM-DD
     */
    public String createEndTime;

    /**
     * 排序字段
     */
    public String orderKey;


    /**
     * 排序类型
     */
    public String orderType;

    /**
     * 	股东关系
     */
    private String shareholderRelationCode;

}
