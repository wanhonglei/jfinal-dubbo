package com.jfinal.plugin.rpc;

import com.jfinal.plugin.rpc.config.RpcReferenceConfig;
import com.jfinal.plugin.rpc.config.RpcServiceConfig;

/**
 * @Author: honglei.wan
 * @Description:rpc 通用接口
 * @Date: Create in 2020/6/17 3:11 下午
 */
public interface Irpc {

	/**
	 * 获取服务
	 * @param serviceClass
	 * @param referenceConfig
	 * @param <T>
	 * @return
	 */
	<T> T serviceObtain(Class<T> serviceClass, RpcReferenceConfig referenceConfig);

	/**
	 * 发布服务
	 * @param interfaceClass
	 * @param object
	 * @param serviceConfig
	 * @param <T>
	 * @return
	 */
	<T> boolean serviceExport(Class<T> interfaceClass, Object object, RpcServiceConfig serviceConfig);

	/**
	 * 启动
	 */
	void onStart();

	/**
	 * 停止
	 */
	void onStop();
}
