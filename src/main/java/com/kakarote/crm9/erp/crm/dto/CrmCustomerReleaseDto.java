package com.kakarote.crm9.erp.crm.dto;

import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CrmCustomerReleaseDto {

    private Long customerId;

    /**
     * 客户关联负责人
     */
    private Long ownerUserId;
    /**
     * 库存类型
     */
    private CustomerStorageTypeEnum storageTypeEnum;
    /**
     * 库存出库限制天数
     */
    private Integer releaseDays;

    private List<CrmBusiness> crmBusinessList;
}
