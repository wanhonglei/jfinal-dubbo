package com.kakarote.crm9.erp.crm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BD跟进的分销商销售数据统计对象
 *
 * @author hao.fu
 * @create 2019/12/3 14:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributorBdSalesStatistic {

    private String crmCustomerId;
    private Integer siteMemberId;
    private String  productCode;
    private String  productName;
    private String  goodsCode;
    private String  goodsSpec;
    private int salesQuantityValue;
}
