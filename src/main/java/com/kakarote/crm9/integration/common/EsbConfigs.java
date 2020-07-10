package com.kakarote.crm9.integration.common;

import com.kakarote.crm9.common.config.JfinalConfig;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/18 10:22 上午
 */
public class EsbConfigs {

    public static String HOST = JfinalConfig.crmProp.get("esb.host");
    /**
     * 快速注册的端口号
     */
    public static String REST_PORT = JfinalConfig.crmProp.get("esb.rest.port");

    public interface Order {
        String PORT = JfinalConfig.crmProp.get("esb.order.port");

        String USER = JfinalConfig.crmProp.get("esb.order.user");

        String PASSWORD = JfinalConfig.crmProp.get("esb.order.password");
    }

    public interface Bops {
        String PORT = JfinalConfig.crmProp.get("esb.bops.port");

        String USER = JfinalConfig.crmProp.get("esb.bops.user");

        String PASSWORD = JfinalConfig.crmProp.get("esb.bops.password");
    }
}
