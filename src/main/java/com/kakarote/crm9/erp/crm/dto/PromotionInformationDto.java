package com.kakarote.crm9.erp.crm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromotionInformationDto {

    /**
     * 客户编号
     */
    private String customerId;
    /**
     * 会员用户ID
     */
    private String siteMemberId;
    /**
     * 会员用户名
     */
    private String siteMemberName;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 手机号
     */
    private String mobile;

    /**
     * 用户类型
     */
    private String customerType;
    /**
     * 是否已验证 1 未认证 2 已认证
     */
    private Integer isAttestation;
    /**
     * 上游分销商ID
     */
    private String pSiteMemberId;
    /**
     * 推广标签
     */
    private String promotionTag;

}
