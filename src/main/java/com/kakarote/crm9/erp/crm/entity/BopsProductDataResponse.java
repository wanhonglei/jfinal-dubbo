package com.kakarote.crm9.erp.crm.entity;

import java.util.List;

/**
 * BopsProductDataResponse.
 *
 * @author yue.li
 * @create 2019/11/15 10:00
 */
public class BopsProductDataResponse {

    /**内容*/
    List<CrmProductDetail> content;

    public List<CrmProductDetail> getContent() {
        return content;
    }

    public void setContent(List<CrmProductDetail> content) {
        this.content = content;
    }
}
