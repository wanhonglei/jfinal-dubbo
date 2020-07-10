package com.kakarote.crm9.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CrmNotesPageRequest class
 *
 * @author yue.li
 * @date 2020/01/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmNotesPageRequest {

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
    private String createStartTime;

    /**
     * 创建结束时间
     */
    private String createEndTime;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 业务ID
     */
    private Integer sceneId;

    /**
     * 记录人域账号IDS
     */
    private String createUserLoginIds;

    /**
     * 创建时间类型
     */
    private Integer createTimeType;

    /**
     * 联系方式
     */
    private String category;

    /**
     * 小记来源
     */
    private Integer channel;

    /**
     * 小计类型
     */
    private String type;
}
