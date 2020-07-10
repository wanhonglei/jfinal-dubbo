package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.jfinal.kit.Kv;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;

/**
 * 分销商推广客户
 *
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:11 下午
 */
public class CustomerDistributorRelatePoolHandler extends BaseCustomerQueryHandler {

    @Override
    protected Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params) {
        //有上游分销商
        params.set("hasPCustomerId", Boolean.TRUE);
        //客户所属部门逻辑
        customerOwnDept(params, paramDto);
        return params;
    }
}
