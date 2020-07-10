package com.kakarote.crm9.integration.service.inter;

/**
 * @Author: honglei.wan
 * @Description:dubbo对外服务接口
 * @Date: Create in 2020/6/8 4:36 下午
 */

public interface CrmInterfaceApi {

	/**
	 * 根据客户名称获取客户详情
	 * @return
	 */
	Object getCustomerInfoByCustomerName();

}
