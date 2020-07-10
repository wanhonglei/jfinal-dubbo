package com.kakarote.crm9.erp.crm.common;

import java.util.Objects;

/**
 * @author liming.guo
 * 操作日志渠道分类
 */
public enum CrmOperateChannelEnum {

    CUSTOMER(1, "客户"),
    BUSINESS(2, "商机"),
    USER(3, "用户");

    private Integer type;

    private String desc;

    CrmOperateChannelEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static CrmOperateChannelEnum getByType(Integer type) {
        if (Objects.isNull(type)) {
            return null;
        }
        for (CrmOperateChannelEnum channelEnum : values()) {
            if (Objects.equals(channelEnum.getType(), type)) {
                return channelEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
