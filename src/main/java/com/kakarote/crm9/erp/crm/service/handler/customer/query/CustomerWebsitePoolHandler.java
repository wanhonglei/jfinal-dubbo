package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.jfinal.kit.Kv;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.utils.CrmDateUtil;

import java.util.Objects;

/**
 * 网站客户池
 *
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:10 下午
 */
public class CustomerWebsitePoolHandler extends BaseCustomerQueryHandler {

    @Override
    protected Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params) {
        //网站池无部门 且 无负责人的创建时间在网站用户保护期外的
        params.set("noDeptId", Boolean.TRUE);
        //无负责人
        params.set("noOwnerUserId", Boolean.TRUE);
        //在网站用户保护期外的
        AdminConfig adminConfig = adminConfigService.queryAdminConfig(CrmConstant.WEBSITE_CUSTOMER_POOL_SETTING);
        if (Objects.nonNull(adminConfig)) {
            params.set("protectTime", CrmDateUtil.getLastParamDay(Integer.parseInt(adminConfig.getValue())));
        }
        return params;
    }
}
