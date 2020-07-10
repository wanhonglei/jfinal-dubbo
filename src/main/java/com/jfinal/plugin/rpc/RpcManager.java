package com.jfinal.plugin.rpc;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.app.config.ConfigManager;
import com.jfinal.plugin.rpc.annotation.RPCBean;
import com.jfinal.plugin.rpc.config.RpcConfig;
import com.jfinal.plugin.rpc.config.RpcServiceConfig;
import com.jfinal.plugin.rpc.dubbo.DubboRpc;
import com.jfinal.plugin.rpc.util.ClassScanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:rpc管理控制类
 * @Date: Create in 2020/6/17 2:55 下午
 */
@Slf4j
public class RpcManager {

	private static RpcManager manager = new RpcManager();

	public static RpcManager me() {
		return manager;
	}

	private Irpc rpc;
	private RpcConfig defaultConfig = ConfigManager.me().get(RpcConfig.class,"rpc");

	public Irpc getRpc() {
		if (rpc == null) {
			if (!defaultConfig.isConfigOk()) {
				throw new RpcException("rpc config is error, please set up rpc.type config value");
			}
			rpc = createRpc(defaultConfig.getType());
		}
		return rpc;
	}


	private static Class[] default_excludes = new Class[]{
			Serializable.class
	};


	public void init() {
		if (!defaultConfig.isConfigOk()) {
			return;
		}

		Irpc rpc = getRpc();
		rpc.onStart();

		if (defaultConfig.isAutoExportEnable()) {
			exportRpcBean(rpc);
		}
	}

	public void stop() {
		if (defaultConfig.isConfigOk()) {
			getRpc().onStop();
		}
	}


	public void exportRpcBean(Irpc rpc) {
		List<Class> classes;
		try {
			classes = ClassScanner.scanClassByAnnotation(RPCBean.class, true);
		} catch (IOException | ClassNotFoundException e) {
			throw new RpcException("exportRPCBean error:" + e.getMessage());
		}

		if (CollectionUtils.isNotEmpty(classes)) {
			return;
		}

		for (Class clazz : classes) {
			RPCBean rpcBean = (RPCBean) clazz.getAnnotation(RPCBean.class);
			Class[] inters = clazz.getInterfaces();
			if (inters == null || inters.length == 0) {
				throw new RpcException(String.format("class[%s] has no interface, can not use @RPCBean", clazz));
			}

			//对某些系统的类 进行排除，例如：Serializable 等
			Class[] excludes = ArrayUtils.addAll(default_excludes, rpcBean.exclude());
			for (Class inter : inters) {
				boolean isContinue = false;
				for (Class ex : excludes) {
					if (ex.isAssignableFrom(inter)) {
						isContinue = true;
						break;
					}
				}

				if (isContinue) {
					continue;
				}

				if (rpc.serviceExport(inter, Aop.get(clazz), new RpcServiceConfig(rpcBean))) {
					if (ConfigManager.me().isDevMode()) {
                        log.info("rpc service[" + inter + "] has exported ok!");
					}
				}
			}
		}
	}

	/**
	 * 创建rpc服务实例
	 * @param type
	 * @return
	 */
	public Irpc createRpc(String type) {
		switch (type) {
			case RpcConfig.TYPE_DUBBO:
				return new DubboRpc();
			default:
				throw new RpcException("wrong rpc type");
		}
	}



}
