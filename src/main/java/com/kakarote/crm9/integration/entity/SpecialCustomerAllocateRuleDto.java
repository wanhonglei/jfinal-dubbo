package com.kakarote.crm9.integration.entity;

import lombok.Data;

/**
 *
 * @author liming.guo
 */
@Data
public class SpecialCustomerAllocateRuleDto {

    /**
     * 会员渠道比如tmall
     */
    private String memberChannel;
    /**
     * 负责人ID
     */
    private Long ownerUserId;
    /**
     * 负责人名称
     */
    private String ownerUserName;

}

