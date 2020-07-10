package com.kakarote.crm9.erp.crm.dto;

import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/18 10:07 上午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmCustomerQueryParamDto implements Serializable {
    private static final long serialVersionUID = 8694039044464980722L;

    /**
     * 场景 Code
     * {@link com.kakarote.crm9.erp.crm.common.scene.CrmCustomerSceneEnum}
     */
    private String sceneCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 官网ID
     */
    private String siteMemberIds;

    /**
     * 客户来源
     * {@link FromSourceEnum}
     */
    private String customerSourceId;

    /**
     * 客户行业
     * 标签系统tagName{@link com.kakarote.crm9.erp.crm.constant.CrmTagConstant#INDUSTRY}
     *
     */
    private String customerIndustryId;

    /**
     * 客户类型
     * 标签系统tagName{@link com.kakarote.crm9.erp.crm.constant.CrmTagConstant#CUSTOMER_TYPE}
     */
    private String customerTypeId;

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
    private String area;

    /**
     * 客户等级
     * 标签系统tagName{@link com.kakarote.crm9.erp.crm.constant.CrmTagConstant#CUSTOMER_GRADE}
     */
    private String customerGradeId;

    /**
     * 分销身份
     */
    private String distributionIdentityId;

    /**
     * 是否是官网客户，0 否，1 是
     */
    private Integer isSiteMember;

    /**
     * 是否有联系人，0 否，1 是
     */
    private Integer hasContacts;

    /**
     * 是否有小记，0 否，1 是
     */
    private Integer hasNotes;

    /**
     * 是否有商机，0 否，1 是
     */
    private Integer hasBusiness;

    /**
     * 进入公海原因
     * 标签系统tagName{@link com.kakarote.crm9.erp.crm.constant.CrmTagConstant#PUBLIC_CUSTOMER_REASON}
     */
    private String putSeasReasonId;

    /**
     * 部门名称
     */
    private Long deptId;

    /**
     * 负责人
     */
    private String ownerUserIds;

    /**
     * 团队成员
     */
    private String teamUserIds;

    /**
     * 创建人
     */
    private String createUserIds;

    /**
     * 创建开始时间
     */
    private String createStartTime;

    /**
     * 创建结束时间
     */
    private String createEndTime;

    /**
     * 更新开始时间
     */
    private String updateStartTime;

    /**
     * 更新结束时间
     */
    private String updateEndTime;

    /**
     * 客户跟进状态 0 未跟进，1 已跟进
     * {@link com.kakarote.crm9.erp.crm.constant.CrmCustomerDisposeStatus}
     */
    private Integer disposeStatus;

    /**
     * 客户库类型 1 考察库，2 关联库
     * {@link com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum}
     */
    private Integer storageType;

    /**
     * 上游分销商
     */
    private Long pCustomerId;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 排序字段
     */
    private String orderKey;

    /**
     * 排序类型，ASC, DESC
     */
    private String orderType;
}
