package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.jfinal.kit.Kv;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.utils.BaseUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * 我创建的客户
 *
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:09 下午
 */
@Slf4j
public class CustomerCreatedByMeHandler extends BaseCustomerQueryHandler {

    @Override
    protected Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params) {
        params.set("createUserIds", Collections.singletonList(BaseUtil.getUserId()));
        //客户所属部门逻辑
        customerOwnDept(params, paramDto);
        return params;
    }
}
