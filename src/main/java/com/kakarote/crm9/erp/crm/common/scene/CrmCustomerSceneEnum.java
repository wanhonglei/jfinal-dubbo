package com.kakarote.crm9.erp.crm.common.scene;

import com.kakarote.crm9.erp.crm.service.handler.customer.query.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/30 5:21 下午
 */
public enum CrmCustomerSceneEnum {
    /**
     * 客户查询场景枚举
     */
    CUSTOMER_ALL("customerAll", "全部客户", CustomerAllHandler.class, 9),
    CUSTOMER_CREATED_BY_ME("customerCreate", "我创建的客户", CustomerCreatedByMeHandler.class, 3),
    DEPT_POOL("customerDept", "部门客户池", CustomerDeptPoolHandler.class, 5),
    WEBSITE_POOL("customerPublic", "网站客户池", CustomerWebsitePoolHandler.class, 6),
    MOBILE_SALE_CUSTOMER("customerTelemarketing", "电销客户", CustomerMobileSaleHandler.class, 7),
    DISTRIBUTOR_RELATE_POOL("customerDistributor", "分销商推广客户", CustomerDistributorRelatePoolHandler.class, 8),
    MINE_CUSTOMER("customerOwn", "我负责的客户", CustomerMineHandler.class, 1),
    MY_SUBORDINATE_CUSTOMER("customerMySub", "下属负责的客户", CustomerMySubordinateHandler.class, 4),
    TAKE_PART_CUSTOMER("customerTakePart", "我参与的客户", CustomerTakePartHandler.class, 2),
    ;

    /**
     * 枚举排序返回
     * @return
     */
    public static CrmCustomerSceneEnum[] orderedList() {
        return Arrays.stream(values()).sorted(Comparator.comparingInt(CrmCustomerSceneEnum::getOrder)).collect(Collectors.toList()).toArray(new CrmCustomerSceneEnum[values().length]);
    }

    public static CrmCustomerSceneEnum findByName(String name) {
        for (CrmCustomerSceneEnum value : values()) {
            if (StringUtils.equals(value.getName(), name)) {
                return value;
            }
        }
        return null;
    }

    public static CrmCustomerSceneEnum findByCode(String code) {
        for (CrmCustomerSceneEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 场景编码
     */
    private String code;
    /**
     * 场景名称
     */
    private String name;
    /**
     * Handler名称
     */
    private Class<?> handlerClass;
    /**
     * 场景顺序
     */
    private Integer order;

    CrmCustomerSceneEnum(String code, String name, Class<?> handlerClass, Integer order) {
        this.code = code;
        this.name = name;
        this.handlerClass = handlerClass;
        this.order = order;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public Integer getOrder() {
        return order;
    }
}
