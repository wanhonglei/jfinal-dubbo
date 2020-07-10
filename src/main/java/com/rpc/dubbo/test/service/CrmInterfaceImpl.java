package com.rpc.dubbo.test.service;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.rpc.annotation.RPCBean;
import com.kakarote.crm9.integration.service.inter.CrmInterfaceApi;

/**
 * @Author: honglei.wan
 * @Description:dubbo 测试dubbo生产者功能
 * @Date: Create in 2020/6/9 9:37 下午
 */
@RPCBean
public class CrmInterfaceImpl implements CrmInterfaceApi {


	@Override
	public Object getSomethingFromProducer() {
		return JSON.parse("dubbo服务提供者 正常。。。");
	}
}
