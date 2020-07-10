package com.jfinal.plugin.app.config;

import cn.hutool.core.util.StrUtil;
import com.JfinalProps;
import com.jfinal.kit.Prop;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/17 4:25 下午
 */
public class ConfigManager {

	/**
	 * 系统配置类缓存
	 */
	private Map<String, Object> configCache = new ConcurrentHashMap<>();

	/**
	 * jfinal-rpc.properties 中的内容
	 */
	private Properties mainProperties;

	public static final JfinalProps JFINAL_PROPS = JfinalProps.getInstance();

	/**
	 * 保证单列
	 */
	private static ConfigManager instance;

	public static ConfigManager me() {
		if (instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	private ConfigManager() {
		init();
	}


	private void init() {

		mainProperties =  JFINAL_PROPS.getProperties();

		//预留属性，区分环境标
		String mode = getConfigValue("jfinal.app.mode");
		if (StringUtils.isNotBlank(mode)) {
			String p = String.format("jfinal-%s.properties", mode);
			mainProperties.putAll(new Prop(p).getProperties());
		}
	}

	/**
	 * 获取配置信息，并创建和赋值clazz实例
	 *
	 * @param clazz  指定的类
	 * @param prefix 配置文件前缀
	 * @param <T>
	 * @return
	 */
	public <T> T get(Class<T> clazz, String prefix) {

		/**
		 * 开发模式下，热加载会导致由于 Config 是不同的 ClassLoader 而导致异常，
		 * 如果走缓存会Class转化异常
		 */
		if (isDevMode()) {
			return createConfigObject(clazz, prefix);
		}

		Object configObject = configCache.get(clazz.getName() + prefix);

		if (configObject == null) {
			synchronized (clazz) {
				if (configObject == null) {
					configObject = createConfigObject(clazz, prefix);
					configCache.put(clazz.getName() + prefix, configObject);
				}
			}
		}

		return (T) configObject;
	}

	/**
	 * 创建一个新的配置对象（Object）
	 *
	 * @param clazz
	 * @param prefix
	 * @param <T>
	 * @return
	 */
	public <T> T createConfigObject(Class<T> clazz, String prefix){
		Object configObject = ConfigUtil.newInstance(clazz);
		List<Method> setMethods = ConfigUtil.getClassSetMethods(clazz);
		if (setMethods != null) {
			for (Method method : setMethods) {

				String key = buildKey(prefix, method);
				String value = getConfigValue(key);

				if (StringUtils.isNotBlank(value)) {
					Object val = ConfigUtil.convert(method.getParameterTypes()[0], value, method.getGenericParameterTypes()[0]);
					if (val != null) {
						try {
							method.invoke(configObject, val);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		return (T) configObject;
	}

	private String buildKey(String prefix, Method method) {
		String key = ConfigUtil.firstCharToLowerCase(method.getName().substring(3));
		if (StringUtils.isNotBlank(prefix)) {
			key = prefix.trim() + "." + key;
		}
		return key;
	}

	public String getConfigValue(String key) {
		return mainProperties.getProperty(key);
	}



	private Boolean devMode = null;

	public boolean isDevMode() {
		if (devMode == null) {
			synchronized (this) {
				if (devMode == null) {
					devMode = JFINAL_PROPS.getBoolean("jfinal.devMode", Boolean.TRUE);
				}
			}
		}
		return devMode;
	}

	public <T> Map<String, T> getConfigModels(Class<T> tClass, String prefix) {
		Map<String, T> objMap = new HashMap<>();


		boolean initDefault = false;


		Set<String> objNames = new HashSet<>();

		String objPrefix = prefix + ".";
		int pointCount = StringUtils.countMatches(prefix, ".");

		for (Map.Entry<Object, Object> entry : mainProperties.entrySet()) {
			if (entry.getKey() == null || StrUtil.isBlank(entry.getKey().toString())) {
				continue;
			}

			String key = entry.getKey().toString().trim();

			//配置来源于 Docker 的环境变量配置
			if (key.contains("_") && Character.isUpperCase(key.charAt(0))){
				key = key.toLowerCase().replace('_','.');
			}

			//初始化默认的配置
			if (!initDefault && key.startsWith(prefix) && StringUtils.countMatches(key, ".") == pointCount + 1) {
				initDefault = true;
				T defaultObj = get(tClass, prefix);
				objMap.put("default", defaultObj);

			}

			if (key.startsWith(objPrefix) && entry.getValue() != null) {
				String[] keySplits = key.split("\\.");
				if (keySplits.length == pointCount + 3) {
					objNames.add(keySplits[pointCount + 1]);
				}
			}
		}

		for (String name : objNames) {
			T obj = get(tClass, objPrefix + name);
			objMap.put(name, obj);
		}

		return objMap;
	}
}
