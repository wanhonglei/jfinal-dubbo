package com.kakarote.crm9.integration.service;

import com.jfinal.aop.Inject;
import com.jfinal.plugin.rpc.annotation.RPCBean;
import com.kakarote.crm9.erp.crm.service.CrmCustomerExtService;
import com.kakarote.crm9.integration.service.inter.CrmInterfaceApi;

/**
 * @Author: honglei.wan
 * @Description:dubbo 对外服务接口实现类
 * @Date: Create in 2020/6/9 9:37 下午
 */
@RPCBean
public class CrmInterfaceImpl implements CrmInterfaceApi {

	@Inject
	private CrmCustomerExtService crmCustomerExtService;

	@Override
	public Object getCustomerInfoByCustomerName() {
		return crmCustomerExtService.queryCrmCustomerExtbySiteMemberId(1212L);
	}
}
