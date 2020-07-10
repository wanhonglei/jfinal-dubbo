package com.jfinal.plugin.rpc.dubbo;

import cn.hutool.core.util.StrUtil;
import com.jfinal.plugin.app.config.ConfigManager;
import com.jfinal.plugin.rpc.RpcUtil;
import com.jfinal.plugin.rpc.config.RpcReferenceConfig;
import com.jfinal.plugin.rpc.config.RpcServiceConfig;
import org.apache.dubbo.config.AbstractConfig;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ArgumentConfig;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.MethodConfig;
import org.apache.dubbo.config.MetricsConfig;
import org.apache.dubbo.config.ModuleConfig;
import org.apache.dubbo.config.MonitorConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.SslConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: honglei.wan
 * @Description:dubbo rpc服务工具类
 * @Date: Create in 2020/6/17 3:44 下午
 */
public class DubboUtil {
	/**
	 * 协议配置
	 */
	private static Map<String, ProtocolConfig> protocolConfigMap = new ConcurrentHashMap<>();
	/**
	 * 注册配置
	 */
	private static Map<String, RegistryConfig> registryConfigMap = new ConcurrentHashMap<>();
	/**
	 * 服务提供者配置
	 */
	private static Map<String, ProviderConfig> providerConfigMap = new ConcurrentHashMap<>();
	/**
	 * 服务消费者配置
	 */
	private static Map<String, ConsumerConfig> consumerConfigMap = new ConcurrentHashMap<>();

	/**
	 * 停止dubbo服务
	 */
	public static void stopDubbo() {
		DubboBootstrap.getInstance().stop();
	}

	/**
	 * 初始化dubbo服务
	 */
	public static void initDubbo() {
		DubboBootstrap dubboBootstrap = DubboBootstrap.getInstance();

		//application 配置
		ApplicationConfig applicationConfig = config(ApplicationConfig.class, "rpc.dubbo.application");
		if (StrUtil.isBlank(applicationConfig.getName())) {
			//TODO 改成你系统的名称
			applicationConfig.setName("DUBBO");
		}
		//默认关闭 qos
		if (applicationConfig.getQosEnable() == null) {
			applicationConfig.setQosEnable(false);
		}

		dubboBootstrap.application(applicationConfig);


		//ssl 配置
		SslConfig sslConfig = config(SslConfig.class, "rpc.dubbo.ssl");
		dubboBootstrap.ssl(sslConfig);


		//monitor 配置
		MonitorConfig monitorConfig = config(MonitorConfig.class, "rpc.dubbo.monitor");
		dubboBootstrap.monitor(monitorConfig);


		//monitor 配置
		MetricsConfig metricsConfig = config(MetricsConfig.class, "rpc.dubbo.metrics");
		dubboBootstrap.metrics(metricsConfig);


		//module 配置
		ModuleConfig moduleConfig = config(ModuleConfig.class, "rpc.dubbo.module");
		dubboBootstrap.module(moduleConfig);


		//元数据 配置
		Map<String, MetadataReportConfig> metadataReportConfigs = configs(MetadataReportConfig.class, "rpc.dubbo.metadata-report");
		if (metadataReportConfigs != null && !metadataReportConfigs.isEmpty()) {
			dubboBootstrap.metadataReports(toList(metadataReportConfigs));
		}

		//配置中心配置
		Map<String, ConfigCenterConfig> configCenterConfigs = configs(ConfigCenterConfig.class, "rpc.dubbo.config-center");
		if (configCenterConfigs != null && !configCenterConfigs.isEmpty()) {
			dubboBootstrap.configCenters(toList(configCenterConfigs));
		}


		//协议 配置
		Map<String, ProtocolConfig> protocolConfigs = configs(ProtocolConfig.class, "rpc.dubbo.protocol");
		if (protocolConfigs != null && !protocolConfigs.isEmpty()) {
			protocolConfigMap.putAll(protocolConfigs);
			dubboBootstrap.protocols(toList(protocolConfigs));
		}

		//服务注册中心 配置
		Map<String, RegistryConfig> registryConfigs = configs(RegistryConfig.class, "rpc.dubbo.registry");
		if (registryConfigs != null && !registryConfigs.isEmpty()) {
			registryConfigMap.putAll(registryConfigs);
			dubboBootstrap.registries(toList(registryConfigs));
		}
		//没有配置注册中心，一般只用于希望此服务网提供直连的方式给客户端使用
		else {
			RegistryConfig config = new RegistryConfig();
			config.setAddress(RegistryConfig.NO_AVAILABLE);
			dubboBootstrap.registry(config);
		}


		//方法参数配置 配置
		Map<String, ArgumentConfig> argumentConfigs = configs(ArgumentConfig.class, "rpc.dubbo.argument");


		//方法配置 配置
		Map<String, MethodConfig> methodConfigs = configs(MethodConfig.class, "rpc.dubbo.method");
		RpcUtil.setChildConfig(methodConfigs, argumentConfigs, "rpc.dubbo.method", "argument");


		//消费者 配置
		Map<String, ConsumerConfig> consumerConfigs = configs(ConsumerConfig.class, "rpc.dubbo.consumer");
		RpcUtil.setChildConfig(consumerConfigs, methodConfigs, "rpc.dubbo.consumer", "method");
		RpcUtil.setChildConfig(consumerConfigs, protocolConfigs, "rpc.dubbo.consumer", "protocol");
		RpcUtil.setChildConfig(consumerConfigs, registryConfigs, "rpc.dubbo.consumer", "registry");


		if (consumerConfigs != null && !consumerConfigs.isEmpty()) {
			consumerConfigMap.putAll(consumerConfigs);
			dubboBootstrap.consumers(toList(consumerConfigs));
		}

		//服务提供者 配置
		Map<String, ProviderConfig> providerConfigs = configs(ProviderConfig.class, "rpc.dubbo.provider");
		RpcUtil.setChildConfig(providerConfigs, methodConfigs, "rpc.dubbo.provider", "method");
		RpcUtil.setChildConfig(providerConfigs, protocolConfigs, "rpc.dubbo.provider", "protocol");
		RpcUtil.setChildConfig(providerConfigs, registryConfigs, "rpc.dubbo.provider", "registry");

		if (providerConfigs != null && !providerConfigs.isEmpty()) {
			providerConfigMap.putAll(providerConfigs);
			dubboBootstrap.providers(toList(providerConfigs));
		}
	}


	public static <T> ReferenceConfig<T> toReferenceConfig(RpcReferenceConfig rpcReferenceConfig, Class<T> interfaceClass) {
		ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
		RpcUtil.copyFields(rpcReferenceConfig, referenceConfig);

		// reference consumer
		if (rpcReferenceConfig.getConsumer() != null) {
			referenceConfig.setConsumer(consumerConfigMap.get(rpcReferenceConfig.getConsumer()));
		}
		// set default consumer
		else {
			for (ConsumerConfig consumerConfig : consumerConfigMap.values()) {
				if (consumerConfig.isDefault() != null && consumerConfig.isDefault()) {
					referenceConfig.setConsumer(consumerConfig);
				}
			}
		}


		//service registry
		if (StrUtil.isNotBlank(rpcReferenceConfig.getRegistry())) {
			referenceConfig.setRegistryIds(rpcReferenceConfig.getRegistry());
		}
		// set default registry
		else {
			for (RegistryConfig registryConfig : registryConfigMap.values()) {
				if (registryConfig.isDefault() != null && registryConfig.isDefault()) {
					referenceConfig.setRegistry(registryConfig);
				}
			}
		}

		return referenceConfig;
	}


	public static <T> ServiceConfig<T> toServiceConfig(RpcServiceConfig rpcServiceConfig,Class<T> interfaceClass) {
		ServiceConfig<T> serviceConfig = new ServiceConfig<>();
		RpcUtil.copyFields(rpcServiceConfig, serviceConfig);

		// service provider
		if (StrUtil.isNotBlank(rpcServiceConfig.getProvider())) {
			serviceConfig.setProviderIds(rpcServiceConfig.getProvider());
		}
		// set default provider
		else {
			for (ProviderConfig providerConfig : providerConfigMap.values()) {
				if (providerConfig.isDefault() != null && providerConfig.isDefault()) {
					serviceConfig.setProvider(providerConfig);
				}
			}
		}

		// service protocol
		if (StrUtil.isNotBlank(rpcServiceConfig.getProtocol())) {
			serviceConfig.setProtocolIds(rpcServiceConfig.getProtocol());
		}
		// set default protocol
		else {
			for (ProtocolConfig protocolConfig : protocolConfigMap.values()) {
				if (protocolConfig.isDefault() != null && protocolConfig.isDefault()) {
					serviceConfig.setProtocol(protocolConfig);
				}
			}
		}

		// service registry
		if (StrUtil.isNotBlank(rpcServiceConfig.getRegistry())) {
			serviceConfig.setRegistryIds(rpcServiceConfig.getRegistry());
		}
		// set default registry
		else {
			for (RegistryConfig registryConfig : registryConfigMap.values()) {
				if (registryConfig.isDefault() != null && registryConfig.isDefault()) {
					serviceConfig.setRegistry(registryConfig);
				}
			}
		}

		return serviceConfig;
	}

	public static ConsumerConfig getConsumer(String name) {
		return consumerConfigMap.get(name);
	}


	public static ProviderConfig getProvider(String name) {
		return providerConfigMap.get(name);
	}


	private static <T> T config(Class<T> clazz, String prefix) {
		return ConfigManager.me().get(clazz, prefix);
	}


	private static <T> Map<String, T> configs(Class<T> clazz, String prefix) {
		return ConfigManager.me().getConfigModels(clazz, prefix);
	}


	private static <T> List<T> toList(Map<String, T> map) {
		List<T> list = new ArrayList<>(map.size());
		for (Map.Entry<String, T> entry : map.entrySet()) {
			AbstractConfig config = (AbstractConfig) entry.getValue();
			config.setId(entry.getKey());
			list.add((T) config);
		}
		return list;
	}
}
