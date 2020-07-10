package com.jfinal.plugin.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: honglei.wan
 * @Description:引用类注解
 * @Date: Create in 2020/6/18 10:52 上午
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface RPCInject {
	/**
	 * Service version, default value is empty string
	 */
	String version() default "";

	/**
	 * Service group, default value is empty string
	 */
	String group() default "";

	/**
	 * Service target URL for direct invocation, if this is specified, then registry center takes no effect.
	 */
	String url() default "";


	/**
	 * Whether to enable generic invocation, default value is false
	 */
	boolean generic() default false;


	/**
	 * Check if service provider is available during boot up, default value is true
	 */
	boolean check() default true;


	/**
	 * Service invocation retry times
	 *
	 * see Constants#DEFAULT_RETRIES
	 */
	int retries() default 2;


	/**
	 * Load balance strategy, legal values include: random, roundrobin, leastactive
	 *
	 * see Constants#DEFAULT_LOADBALANCE
	 */
	String loadbalance() default "random";

	/**
	 * Whether to enable async invocation, default value is false
	 */
	boolean async() default false;

	/**
	 * Maximum active requests allowed, default value is 0
	 */
	int actives() default 0;


	/**
	 * Timeout value for service invocation, default value is 0
	 */
	int timeout() default 0;

	/**
	 * Application associated name
	 */
	String application() default "";

	/**
	 * Module associated name
	 */
	String module() default "";


	/**
	 * Consumer associated name
	 */
	String consumer() default "";

	/**
	 * Monitor associated name
	 */
	String monitor() default "";

	/**
	 * Registry associated name
	 */
	String[] registry() default {};

	/**
	 * The communication protocol of Dubbo Service
	 *
	 * @return the default value is ""
	 * @since 2.6.6
	 */
	String protocol() default "";

	/**
	 * Service tag name
	 */
	String tag() default "";

	/**
	 * The id
	 *
	 * @return default value is empty
	 * @since 2.7.3
	 */
	String id() default "";
}
