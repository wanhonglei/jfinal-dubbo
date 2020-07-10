package com.kakarote.crm9.erp.crm.common;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author liming.guo
 * 渠道事件分类
 */
public enum CrmOperateChannelEventEnum {

    CUSTOMER_TRANSFER("customer_transfer", "客户分派"),
    CUSTOMER_ADD("customer_add", "客户新增"),
    CUSTOMER_EDIT("customer_edit", "客户编辑"),
    CUSTOMER_UPLOAD_BY_EXCEL("customer_by_excel", "客户excel导入"),
    CUSTOMER_PUT_PUBLIC_POOL("customer_putpublicpool", "客户放入网站客户池"),
    CUSTOMER_PUT_DEPT_POOL("customer_putdeptpool", "客户放入部门客户池"),
    CUSTOMER_CONFIRM_RECEIVE("customer_confirmreceive", "客户确认领取"),
    CUSTOMER_CRON_PUT_PUBLIC_POOL("customer_cron_putpublicpool", "定时任务将客户放入公海"),
    CUSTOMER_AUTO_PUT_PUBLIC_POOL("customer_auto_putpublicpool", "客户自动释放回公海"),
    CUSTOMER_AUTO_ALLOCATION("customer_auto_allocation", "客户自动分发"),
    CUSTOMER_MEMBER_CHANNEL_TRANSFER("customer_member_channel_transfer", "会员注册渠道分派负责人"),
    LEADS_TO_CUSTOMER("leads_to_customer", "线索转客户"),
    CUSTOMER_MQ_DISTRIBUTOR_AUDIT("mq_distributor_audit", "分销商认证变更负责人"),
    BUSINESS_TRANSFER("business_transfer", "商机分派"),
    BUSINESS_AUTO_PUT_PUBLIC_POOL("business_auto_putpublicpool", "商机自动释放回公海"),
    USER_EDIT("user_edit", "员工信息编辑");

    private String name;

    private String desc;

    CrmOperateChannelEventEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static CrmOperateChannelEventEnum getByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (CrmOperateChannelEventEnum channelEventEnum : values()) {
            if (Objects.equals(channelEventEnum.getName(), name)) {
                return channelEventEnum;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
