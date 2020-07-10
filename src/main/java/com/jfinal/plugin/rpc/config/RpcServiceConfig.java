package com.jfinal.plugin.rpc.config;

import com.jfinal.plugin.rpc.RpcUtil;
import com.jfinal.plugin.rpc.annotation.RPCBean;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: honglei.wan
 * @Description:服务配置类（productor）
 * @Date: Create in 2020/6/17 2:55 下午
 */
@Data
public class RpcServiceConfig implements Serializable {

    /**
     * Service version, default value is empty string
     */
    private String version;

    /**
     * Service group, default value is empty string
     */
    private String group;

    /**
     * Service path, default value is empty string
     */
    private String path;

    /**
     * Whether to export service, default value is true
     */
    private Boolean export;

    /**
     * Service token, default value is false
     */
    private String token;

    /**
     * Whether the service is deprecated, default value is false
     */
    private Boolean deprecated;


    /**
     * Whether to register the service to register center, default value is true
     */
    private Boolean register;

    /**
     * Service weight value, default value is 0
     */
    private Integer weight;

    /**
     * Service doc, default value is ""
     */
    private String document;


    /**
     * Service invocation retry times
     */
    private int retries;

    /**
     * Load balance strategy, legal values include: random, roundrobin, leastactive
     */
    private String loadbalance;


    /**
     * Application bean name
     */
    private String application;

    /**
     * Module bean name
     */
    private String module;

    /**
     * Provider bean name
     */
    private String provider;

    /**
     * Protocol bean names
     */
    private String protocol;

    /**
     * Monitor bean name
     */
    private String monitor;

    /**
     * Registry bean name
     */
    private String registry;

    /**
     * Service tag name
     */
    private String tag;

    public RpcServiceConfig(RPCBean bean) {
        RpcUtil.appendAnnotation(RPCBean.class, bean, this);
    }

}
