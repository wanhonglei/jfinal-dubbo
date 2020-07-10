package com.kakarote.crm9.erp.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 可验证结果
 * @author liming.guo
 */
@Data
@Builder
public class CrmBusinessStatusVerificationDto {

    /**
     * 可验证结果编号
     */
    private Long verificationId;
    /**
     * 可验证结果名称
     */
    private String verificationName;


}
