package com.kakarote.crm9.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 上下文
 * @Author: haihong.wu
 * @Date: 2020/6/1 9:36 上午
 */
public class CrmContext {

    public static final String CRM_CUSTOMER = "CRM_CUSTOMER";
    public static final String CRM_CUSTOMER_AFTER_SPLIT = "CRM_CUSTOMER_AFTER_SPLIT";
    public static final String DISTRIBUTION_RELATION = "DISTRIBUTION_RELATION";
    public static final String SITE_MEMBER = "SITE_MEMBER";

    private Map<String, Object> attrMap = new HashMap<>();

    public void put(String key, Object attr) {
        attrMap.put(key, attr);
    }

    public Object get(String key) {
        return attrMap.get(key);
    }

    public <T> T getAs(String key) {
        return (T) attrMap.get(key);
    }
}
