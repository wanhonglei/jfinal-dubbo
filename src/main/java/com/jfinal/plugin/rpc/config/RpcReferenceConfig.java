/**
 * Copyright (c) 2015-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jfinal.plugin.rpc.config;

import com.jfinal.plugin.rpc.RpcUtil;
import com.jfinal.plugin.rpc.annotation.RPCInject;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: honglei.wan
 * @Description:rpc引用配置类（consumer）
 * @Date: Create in 2020/6/17 2:55 下午
 */
@Data
public class RpcReferenceConfig implements Serializable {

    /**
     * Service version, default value is empty string
     */
    private String version;

    /**
     * Service group, default value is empty string
     */
    private String group;

    /**
     * Service target URL for direct invocation, if this is specified, then registry center takes no effect.
     */
    private String url;


    /**
     * Whether to enable generic invocation, default value is false
     */
    private Boolean generic;


    /**
     * Check if service provider is available during boot up, default value is true
     */
    private Boolean check;


    /**
     * Service invocation retry times
     * <p>
     * see Constants#DEFAULT_RETRIES
     */
    private Integer retries;


    /**
     * Load balance strategy, legal values include: random, roundrobin, leastactive
     * <p>
     * see Constants#DEFAULT_LOADBALANCE
     */
    private String loadbalance;

    /**
     * Whether to enable async invocation, default value is false
     */
    private Boolean async;

    /**
     * Maximum active requests allowed, default value is 0
     */
    private Integer actives;


    /**
     * Timeout value for service invocation, default value is 0
     */
    private Integer timeout;

    /**
     * Application associated name
     */
    private String application;

    /**
     * Module associated name
     */
    private String module;


    /**
     * Consumer associated name
     */
    private String consumer;

    /**
     * Monitor associated name
     */
    private String monitor;

    /**
     * Registry associated name
     */
    private String registry;

    /**
     * the default value is ""
     */
    private String protocol;

    /**
     * Service tag name
     */
    private String tag;

    /**
     * The id
     * <p>
     * default value is empty
     */
    private String id;

    public RpcReferenceConfig(RPCInject inject) {
        RpcUtil.appendAnnotation(RPCInject.class, inject, this);
    }

}
