package com.kakarote.crm9.erp.crm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/17 7:43 下午
 */
@Data
public class BackendOrderPager implements Serializable {
    private static final long serialVersionUID = 9056309575271418229L;
    private Integer totalPages;
    private List<BackendOrderInfo> content;
}
