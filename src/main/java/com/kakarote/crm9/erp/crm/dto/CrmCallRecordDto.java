package com.kakarote.crm9.erp.crm.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/1/8 16:42
 */
@Getter
@Setter
public class CrmCallRecordDto {
    /**
     * 通话记录关联的业务类型：crm_leads 线索 crm_customer 客服
     */
    private String types;
    /**
     * 关联业务表主键
     */
      private Integer typesId;
    /**
     * 小记ID，关联小记时必传
     */
    private Integer recordId;
    /**
     * 记录类型：0 未关联 1 已关联 不传或者传null 查全部
     */
    private Integer   recordType;
    /**
     * 查询类型： 1 记录tab 2 新建小记关联 3 编辑小记关联
     */
    private Integer queryType;
    
    private Integer pageNo;
    
    private Integer pageSize;
    
    /**
     * o-分页；1-不要分页
     */
    private Integer isPage;
}
