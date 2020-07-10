package com.jfinal.plugin.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: honglei.wan
 * @Description:服务类注解
 * @Date: Create in 2020/6/17 3:00 下午
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RPCBean {

	/**
	 * Service version, default value is empty string
	 */
	String version() default "";

	/**
	 * Service group, default value is empty string
	 */
	String group() default "";

	/**
	 * Service path, default value is empty string
	 */
	String path() default "";

	/**
	 * Whether to export service, default value is true
	 */
	boolean export() default true;

	/**
	 * Service token, default value is false
	 */
	String token() default "";

	/**
	 * Whether the service is deprecated, default value is false
	 */
	boolean deprecated() default false;


	/**
	 * Whether to register the service to register center, default value is true
	 */
	boolean register() default true;

	/**
	 * Service weight value, default value is 0
	 */
	int weight() default 0;

	/**
	 * Service doc, default value is ""
	 */
	String document() default "";


	/**
	 * Service invocation retry times
	 *
	 */
	int retries() default 2;

	/**
	 * Load balance strategy, legal values include: random, roundrobin, leastactive
	 *
	 */
	String loadBalance() default "random";

	/**
	 * Application bean name
	 */
	String application() default "";

	/**
	 * Module bean name
	 */
	String module() default "";

	/**
	 * Provider bean name
	 */
	String provider() default "";

	/**
	 * Protocol bean names
	 */
	String[] protocol() default {};

	/**
	 * Monitor bean name
	 */
	String monitor() default "";

	/**
	 * Registry bean name
	 */
	String[] registry() default {};

	/**
	 * Service tag name
	 */
	String tag() default "";


	/**
	 * 当一个Service类实现多个接口的时候，可以通过这个排除不暴露某个实现接口
	 * @return
	 */
	Class[] exclude() default Void.class;
}
