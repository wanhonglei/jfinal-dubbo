package com.kakarote.crm9.erp.crm.common;

import java.util.Objects;

/**
 * 商机阶段启用状态
 *
 * @author liming.guo
 */
public enum CrmBusinessStatusOpenEnum {

    OPEN(1, "启用"),
    CLOSE(2, "封存");

    private Integer status;

    private String desc;

    CrmBusinessStatusOpenEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static CrmBusinessStatusOpenEnum getByStatus(Integer status) {
        if (Objects.isNull(status)) {
            return null;
        }
        for (CrmBusinessStatusOpenEnum statusOpenEnum : values()) {
            if (Objects.equals(statusOpenEnum.getStatus(), status)) {
                return statusOpenEnum;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

}
