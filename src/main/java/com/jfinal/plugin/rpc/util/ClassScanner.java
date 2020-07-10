package com.jfinal.plugin.rpc.util;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.app.config.ConfigManager;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;

/**
 * @Author: honglei.wan
 * @Description:class扫描工具类
 * @Date: Create in 2020/6/18 9:36 上午
 */
public class ClassScanner {

	private static final Set<Class> APP_CLASSES_CACHE = new HashSet<>();

	private static String classPath = PathKit.getRootClassPath();

	private static String DUBBO_BASE_PACKAGE = ConfigManager.me().getConfigValue("rpc.dubbo_base_package");

	public static List<Class> scanClassByAnnotation(Class annotationClass, boolean isInstantiable) throws IOException, ClassNotFoundException {
		initIfNecessary();

		List<Class> list = new ArrayList<>();
		for (Class clazz : APP_CLASSES_CACHE) {
			Annotation annotation = clazz.getAnnotation(annotationClass);
			if (annotation == null) {
				continue;
			}

			if (isInstantiable && !isInstantiable(clazz)) {
				continue;
			}

			list.add(clazz);
		}

		return list;
	}

	private static void initIfNecessary() throws IOException, ClassNotFoundException {
		if (StringUtils.isBlank(DUBBO_BASE_PACKAGE)){
			throw new IOException("do not find dubbo_base_package config");
		}
		if (APP_CLASSES_CACHE.isEmpty()) {
			scanClass(classPath);
		}
	}

	private static boolean isInstantiable(Class clazz) {
		return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
	}

	private static void scanClass(String path) throws ClassNotFoundException, IOException {
		File[] files = new File(path).listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				if (file.isDirectory()) {
					scanClass(file.getAbsolutePath());
				} else if (file.getName().endsWith(".class")) {
					int start = classPath.length();
					int end = file.toString().length() - ".class".length();
					String classFile = file.toString().substring(start + 1, end);
					String className = classFile.replace(File.separator, ".");
					APP_CLASSES_CACHE.add(Class.forName(className));
				}
			}
		}
		Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(DUBBO_BASE_PACKAGE.replace(".", "/"));
		while (urlEnumeration.hasMoreElements()) {
			URL url = urlEnumeration.nextElement();
			if ("jar".equalsIgnoreCase(url.getProtocol())) {
				//转换为JarURLConnection
				JarURLConnection connection = (JarURLConnection) url.openConnection();
				if (connection == null) {
					return;
				}
				if (connection.getJarFile() == null) {
					return;
				}
				//得到该jar文件下面的类实体
				Enumeration<JarEntry> jarEntryEnumeration = connection.getJarFile().entries();
				while (jarEntryEnumeration.hasMoreElements()) {
					String jarEntryName = jarEntryEnumeration.nextElement().getName();
					//这里我们需要过滤不是class文件和不在basePack包名下的类
					if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/", ".").startsWith(DUBBO_BASE_PACKAGE)) {
						String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
						APP_CLASSES_CACHE.add(Class.forName(className));
					}
				}
			}
		}
	}
}
