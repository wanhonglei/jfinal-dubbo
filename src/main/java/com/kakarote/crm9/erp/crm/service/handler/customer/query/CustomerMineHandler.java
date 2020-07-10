package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.jfinal.kit.Kv;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.utils.BaseUtil;

import java.util.Collections;

/**
 * 我负责的客户
 *
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:11 下午
 */
public class CustomerMineHandler extends BaseCustomerQueryHandler {

    @Override
    protected Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params) {
        //负责人为自己
        params.set("ownerUserIds", Collections.singletonList(BaseUtil.getUserId()));
        return params;
    }

}
