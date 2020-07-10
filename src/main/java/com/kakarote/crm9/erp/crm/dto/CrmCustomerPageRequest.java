package com.kakarote.crm9.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CrmCustomerPageRequest class
 *
 * @author yue.li
 * @date 2019/12/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerPageRequest {

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 客户等级
     */
    private String customerGrade;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 负责人ID
     */
    private Integer ownerUserId;

    /**
     * 创建开始时间
     */
    private String startCreateTime;

    /**
     * 创建结束时间
     */
    private String endCreateTime;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 业务ID
     */
    private Integer sceneId;

    /**
     * 客户ID
     */
    private Integer customerId;

    /**
     * 负责人域账号IDS
     */
    private String ownerUserLoginIds;

    /**
     * 创建时间类型
     */
    private Integer createTimeType;
}
