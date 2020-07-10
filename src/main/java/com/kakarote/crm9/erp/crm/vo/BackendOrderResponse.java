package com.kakarote.crm9.erp.crm.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/16 6:03 下午
 */
@Data
public class BackendOrderResponse implements Serializable {
    private static final long serialVersionUID = 6926778674099355672L;
    private Integer code;
    private String msg;
    private BackendOrderPager data;
}
