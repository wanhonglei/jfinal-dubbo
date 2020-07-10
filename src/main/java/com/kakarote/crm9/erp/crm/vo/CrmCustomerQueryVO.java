package com.kakarote.crm9.erp.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/30 5:34 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerQueryVO implements Serializable {
    private static final long serialVersionUID = 5789341249963880369L;

    /**
     * 场景ID
     */
    private Integer sceneId;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 客户名称
     */
    private String search;

    /**
     * 跟进状态
     */
    private Integer disposeStatus;

    /**
     * 库容类型
     */
    private Integer storageType;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序类型 desc asc
     */
    private String orderNum;

    /**
     * 条件
     */
    private List<CrmCustomerQueryConditionVO> conditions;
}
