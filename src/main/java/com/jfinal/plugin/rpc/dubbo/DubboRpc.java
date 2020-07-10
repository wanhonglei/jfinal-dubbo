package com.jfinal.plugin.rpc.dubbo;

import cn.hutool.core.util.StrUtil;
import com.jfinal.plugin.rpc.BaseRpc;
import com.jfinal.plugin.rpc.config.RpcReferenceConfig;
import com.jfinal.plugin.rpc.config.RpcServiceConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.ServiceConfig;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/17 3:10 下午
 */
public class DubboRpc extends BaseRpc {

	@Override
	public void onStart() {
		DubboUtil.initDubbo();
		setStarted(true);
	}

	@Override
	public void onStop() {
		DubboUtil.stopDubbo();
	}

	@Override
	public <T> T onServiceCreate(Class<T> interfaceClass, RpcReferenceConfig config) {
		ReferenceConfig<T> reference = DubboUtil.toReferenceConfig(config,interfaceClass);
		reference.setInterface(interfaceClass);

		String directUrl = rpcConfig.getUrl(interfaceClass.getName());
		if (StrUtil.isNotBlank(directUrl)) {
			reference.setUrl(directUrl);
		}

		String consumer = rpcConfig.getConsumer(interfaceClass.getName());
		if (consumer != null) {
			reference.setConsumer(DubboUtil.getConsumer(consumer));
		}

		if (reference.getGroup() == null) {
			reference.setGroup(rpcConfig.getGroup(interfaceClass.getName()));
		}

		if (reference.getVersion() == null) {
			reference.setVersion(rpcConfig.getVersion(interfaceClass.getName()));
		}

		return reference.get();
	}


	@Override
	public <T> boolean serviceExport(Class<T> interfaceClass, Object object, RpcServiceConfig config) {
		ServiceConfig<T> service = DubboUtil.toServiceConfig(config,interfaceClass);
		service.setInterface(interfaceClass);
		service.setRef((T) object);

		String provider = rpcConfig.getProvider(interfaceClass.getName());
		if (provider != null) {
			service.setProvider(DubboUtil.getProvider(provider));
		}

		if (service.getGroup() == null) {
			service.setGroup(rpcConfig.getGroup(interfaceClass.getName()));
		}

		if (service.getVersion() == null) {
			service.setVersion(rpcConfig.getVersion(interfaceClass.getName()));
		}

		service.export();
		return true;
	}
}
