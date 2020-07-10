package com.kakarote.crm9.erp.crm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CrmBusinessExcel class
 *
 * @author yue.li
 * @date 2020/04/09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmBusinessExcel {

    /**
     * 商机名称
     */
    private String businessName;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 负责人
     */
    private String ownerUserName;

    /**
     * 负责人ID
     */
    private Integer ownerUserId;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 事业部ID
     */
    private Integer deptId;

    /**
     * 客户ID
     */
    private Long customerId;
}
