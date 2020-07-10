package com.jfinal.plugin.rpc;


import cn.hutool.core.util.StrUtil;
import com.jfinal.plugin.app.config.ConfigManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: honglei.wan
 * @Description:rpc帮助类
 * @Date: Create in 2020/6/17 4:43 下午
 */
public class RpcUtil {

	/**
	 * 根据注解来设置对象内容，参考 dubbo 下的 AbstractConfig
	 * 参考 org.apache.dubbo.config.AbstractConfig#appendAnnotation
	 *
	 * @param annotationClass
	 * @param annotation
	 * @param appendTo
	 */
	public static void appendAnnotation(Class<?> annotationClass, Object annotation, Object appendTo) {
		Method[] methods = annotationClass.getMethods();
		for (Method method : methods) {
			if (method.getDeclaringClass() != Object.class
					&& method.getReturnType() != void.class
					&& !"toString".equals(method.getName())
					&& !"hashCode".equals(method.getName())
					&& !"annotationType".equals(method.getName())
					&& method.getParameterTypes().length == 0
					&& Modifier.isPublic(method.getModifiers())
					&& !Modifier.isStatic(method.getModifiers())) {
				try {
					String property = method.getName();
					if ("interfaceClass".equals(property) || "interfaceName".equals(property)) {
						property = "interface";
					}
					String setter = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
					Object value = method.invoke(annotation);
					if (value != null && !value.equals(method.getDefaultValue())) {
						Method setterMethod;
						if ("filter".equals(property) || "listener".equals(property) || "registry".equals(property)) {
							value = StrUtil.join( ",", (String[]) value);
							setterMethod = getMethod(appendTo.getClass(), setter, String.class);
						} else if ("parameters".equals(property)) {
							value = string2Map((String) value);
							setterMethod = getMethod(appendTo.getClass(), setter, Map.class);
						} else {
							setterMethod = getMethod(appendTo.getClass(), setter, method.getReturnType());
						}
						if (setterMethod != null) {
							setterMethod.invoke(appendTo, value);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}


	/**
	 * copy object field value to other
	 *
	 * @param copyFrom
	 * @param copyTo
	 */
	public static void copyFields(Object copyFrom, Object copyTo) {
		Field[] fields = copyFrom.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				String setterName = "set" + firstCharToUpperCase(field.getName());
				Method method = getMethod(copyTo.getClass(), setterName, field.getType());

				if (method != null) {
					field.setAccessible(true);
					Object value = field.get(copyFrom);
					if (value != null && !"0".equals(value) && !"".equals(value)) {
						method.invoke(copyTo, value);
					}
				}
			} catch (Exception ex) {
				// ignore
			}
		}
	}


	private static Method getMethod(Class clazz, String methodName, Class type) {
		try {
			return clazz.getMethod(methodName, getBoxedClass(type));
		} catch (NoSuchMethodException e) {
			try {
				return clazz.getMethod(methodName, type);
			} catch (NoSuchMethodException ex) {

			}
		}

		return null;
	}


	/**
	 * 设置子节点配置，比如 ProviderConfig 下的 MethodsConfig ，或者 MethodConfig 下的 ArgumentConfig 等
	 *
	 * @param appendTo   要设置的对象
	 * @param dataSource 设置子节点的数据源
	 * @param prefix     要设置对象的配置前缀（*rpc.properties 下的配置）
	 * @param arrName    要设置对象的属性名
	 * @param <T>
	 * @param <F>
	 */
	public static <T, F> void setChildConfig(Map<String, T> appendTo, Map<String, F> dataSource, String prefix, String arrName) {
		if (appendTo != null && !appendTo.isEmpty()) {
			for (Map.Entry<String, T> entry : appendTo.entrySet()) {

				//"rpc.dubbo.method.argument"
				//"rpc.dubbo.method."+entry.getKey()+".argument";
				String configKey = "default".equals(entry.getKey())
						? prefix + "." + arrName
						: prefix + "." + entry.getKey() + "." + arrName;

				String configValue = ConfigManager.me().getConfigValue(configKey);
				if (StrUtil.isNotBlank(configValue)) {
					List<F> argCfgList = new ArrayList<>();
					Set<String> arguments = splitToSet(configValue,",");
					for (String arg : arguments) {
						F fillObj = dataSource.get(arg);
						if (fillObj != null) {
							argCfgList.add(fillObj);
						}
					}
					if (!argCfgList.isEmpty()) {
						try {
							//setArguments/setMethods/setRegistries
							String setterMethodName = "registry".equals(arrName)
									? "setRegistries"
									: "set" + firstCharToUpperCase(arrName) + "s";

							Method method = entry.getValue().getClass().getMethod(setterMethodName, List.class);
							method.invoke(entry.getValue(), argCfgList);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private static Class<?> getBoxedClass(Class<?> c) {
		if (c == int.class) {
			c = Integer.class;
		} else if (c == boolean.class) {
			c = Boolean.class;
		} else if (c == long.class) {
			c = Long.class;
		} else if (c == float.class) {
			c = Float.class;
		} else if (c == double.class) {
			c = Double.class;
		} else if (c == char.class) {
			c = Character.class;
		} else if (c == byte.class) {
			c = Byte.class;
		} else if (c == short.class) {
			c = Short.class;
		}
		return c;
	}

	private static Map<String,String> string2Map(String s){
		Map<String,String> map =  new HashMap<>(10);
		String[] strings = s.split(",");
		for (String kv : strings) {
			if (kv != null && kv.contains(":")) {
				String[] keyValue = kv.split(":");
				if (keyValue.length == 2) {
					map.put(keyValue[0], keyValue[1]);
				}
			}
		}
		return map;
	}

	private static String firstCharToUpperCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'a' && firstChar <= 'z') {
			char[] arr = str.toCharArray();
			arr[0] = (char)(arr[0] - 32);
			return new String(arr);
		} else {
			return str;
		}
	}

	/**
	 * 把字符串拆分成一个set
	 *
	 * @param src
	 * @param regex
	 * @return
	 */
	private static Set<String> splitToSet(String src, String regex) {
		if (src == null) {
			return null;
		}

		String[] strings = src.split(regex);
		Set<String> set = new LinkedHashSet<>();
		for (String s : strings) {
			if (StrUtil.isBlank(s)) {
				continue;
			}
			set.add(s.trim());
		}
		return set;
	}

}
