package com.kakarote.crm9.erp.crm.entity;

/**
 * BopsProductResponse.
 *
 * @author yue.li
 * @create 2019/11/15 10:00
 */
public class BopsProductResponse {

    /**code编码*/
    private String code;

    /**data数据*/
    private BopsProductDataResponse data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BopsProductDataResponse getData() {
        return data;
    }

    public void setData(BopsProductDataResponse data) {
        this.data = data;
    }
}
