package com.kakarote.crm9.integration.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/24 2:27 下午
 */
@Data
public class DistributorBindContentDto implements Serializable {

    private static final long serialVersionUID = -8561167116113232803L;
    /**
     * 下游userId
     */
    private Long affiliateUserId;

    /**
     * 下游用户手机号
     */
    private String affiliateMobile;

    /**
     * 下游用户网站会员名
     */
    private String affiliateUserName;

    /**
     * 下游用户真实姓名
     */
    private String affiliateRealName;

    /**
     * 用户类型
     *
     * @see com.qxwz.venus.api.v2.enums.MemberType
     */
    private Integer userType;

    /**
     * 认证状态
     *
     * @see com.qxwz.venus.api.v2.enums.MemberAuditStatus
     */
    private Integer auditStatus;

    /**
     * 推广标签
     *
     * @see com.qxwz.venus.enums.AmbMutexEnums
     */
    private String ambTag;

    /**
     * 下游伙伴类型
     */
    private String affiliateAmbassadorType;

    /**
     * 生态伙伴类型
     */
    private String ambassadorType;

    /**
     * 绑定类型：解绑/绑定
     */
    private String bindType;

    /**
     * 上游分销商userId
     */
    private Long userId;


    /**
     * 生态伙伴等级
     */
    private Integer ambLevel;
}
