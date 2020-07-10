package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.erp.crm.common.scene.CrmCustomerSceneEnum;

import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/7 4:48 下午
 */
public class CustomerQueryHandlers {

    public static BaseCustomerQueryHandler getHandler(CrmCustomerSceneEnum sceneEnum) {
        if (Objects.isNull(sceneEnum)) {
            return null;
        }
        return (BaseCustomerQueryHandler) Aop.get(sceneEnum.getHandlerClass());
    }
}
