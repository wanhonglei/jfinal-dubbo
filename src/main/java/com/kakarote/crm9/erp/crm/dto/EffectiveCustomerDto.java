package com.kakarote.crm9.erp.crm.dto;

import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import lombok.Data;

import java.util.Date;

@Data
public class EffectiveCustomerDto {

    /**
     * 客户编号
     */
    private Long customerId;

    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 客户关联负责人
     */
    private Long ownerUserId;
    /**
     * 客户库存类型  1 考察库 2 关联库
     * {@link CustomerStorageTypeEnum}
     */
    private Integer storageType;
    /**
     * 客户领取时间
     */
    private Date ownerTime;


}
