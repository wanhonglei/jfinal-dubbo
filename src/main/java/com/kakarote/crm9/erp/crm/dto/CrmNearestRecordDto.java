package com.kakarote.crm9.erp.crm.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CrmNearestRecordDto {

    private Long recordId;

    private Long customerId;

    private Long userId;

    private Date createTime;
}
