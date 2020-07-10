package com.kakarote.crm9.erp.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:49 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerQueryConditionVO implements Serializable {
    private static final long serialVersionUID = 3743684709639121177L;

    private String condition;
    private String value;
    private String formType;
    private String name;
    private String start;
    private String end;
}
