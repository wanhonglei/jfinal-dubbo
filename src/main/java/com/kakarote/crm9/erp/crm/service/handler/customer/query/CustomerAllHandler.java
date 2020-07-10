package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.jfinal.kit.Kv;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;

/**
 * 全部客户
 *
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:08 下午
 */
public class CustomerAllHandler extends BaseCustomerQueryHandler {

    @Override
    protected Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params) {
        //客户所属部门逻辑
        customerOwnDept(params, paramDto);
        return params;
    }
}
