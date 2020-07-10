package com.kakarote.crm9.erp.crm.service.handler.customer.sync;

import com.jfinal.aop.Aop;

/**
 * 客户同步Handler
 *
 * @Author: haihong.wu
 * @Date: 2020/6/1 9:27 上午
 */
public class CustomerSyncHandler {

    public static BaseSyncHandler getHandler(SyncHandlersEnum handlersEnum) {
        return (BaseSyncHandler) Aop.get(handlersEnum.getClz());
    }

    public enum SyncHandlersEnum {
        Website_Company_Audit("企业用户官网认证", WebsiteCompanyAuditHandler.class),
        Website_Personal_Audit("个人用户官网认证", WebsitePersonalAuditHandler.class),
        Distributor_Audit("分销商认证", DistributorAuditHandler.class),
        ;
        private String name;
        private Class<?> clz;

        SyncHandlersEnum(String name, Class<?> clz) {
            this.name = name;
            this.clz = clz;
        }

        public String getName() {
            return name;
        }

        public Class<?> getClz() {
            return clz;
        }
    }
}
