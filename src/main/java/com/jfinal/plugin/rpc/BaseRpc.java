package com.jfinal.plugin.rpc;

import com.jfinal.plugin.app.config.ConfigManager;
import com.jfinal.plugin.rpc.config.RpcConfig;
import com.jfinal.plugin.rpc.config.RpcReferenceConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/17 3:33 下午
 */
public abstract class BaseRpc implements Irpc{

	/**
	 * rpc服务列表
	 */
	protected static final Map<String, Object> OBJECT_CACHE = new ConcurrentHashMap<>();

	/**
	 * rpc配置
	 */
	protected static RpcConfig rpcConfig = ConfigManager.me().get(RpcConfig.class,"rpc");

	/**
	 * 启动状态
	 */
	private volatile boolean started = false;

	@Override
	public <T> T serviceObtain(Class<T> interfaceClass, RpcReferenceConfig config) {
		/*
		 * 没有指定group，默认取当前环境的环境标
		 */
		if (StringUtils.isBlank(config.getGroup())){
			//TODO 改成系统的环境表配置
			config.setGroup("DEFAULT");
		}

		/*
		 * 没有指定version，默认取当前默认版本号
		 */
		if (StringUtils.isBlank(config.getVersion())){
			config.setVersion(rpcConfig.getDefaultVersion());
		}

		String key = interfaceClass.getName() + ":" + config.getGroup() + ":" + config.getVersion();
		T object = (T) OBJECT_CACHE.get(key);
		if (object == null) {
			synchronized (this) {
				if (OBJECT_CACHE.get(key) == null) {

					callStartMethodIfNecessary();

					object = onServiceCreate(interfaceClass, config);
					if (object != null) {
						OBJECT_CACHE.put(key, object);
					}
				}
			}
		}
		return object;
	}


	@Override
	public void onStart() {
		setStarted(true);
	}


	@Override
	public void onStop() {

	}

	/**
	 * 创建service服务列表
	 * @param serviceClass
	 * @param config
	 * @param <T>
	 * @return
	 */
	public abstract <T> T onServiceCreate(Class<T> serviceClass, RpcReferenceConfig config);

	/**
	 * onStart 方法是在 app 启动完成后，JFinal 主动去调用的
	 * 但是，在某些场景可能存在没有等 app 启动完成就去获取 Service 的情况
	 * 此时，需要主动先调用下 onStart 方法
	 */
	private void callStartMethodIfNecessary() {
		if (!started) {
			synchronized (this) {
				if (!started) {
					onStart();
				}
			}
		}
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
}
