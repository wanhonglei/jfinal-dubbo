package com.kakarote.crm9.erp.crm.common;

/**
 * CrmCustomerDateEnum class
 *
 * @author yue.li
 * @date 2020/01/13
 */
public enum CrmCustomerDateEnum {
    /**
     * CrmCustomerDateEnum Enum
     */
    ONE_WEEK_KEY(0,"最近一周"),
    TWO_WEEK_KEY(1,"最近两周"),
    ONE_MONTH_KEY(2,"最近一个月");

    private Integer id;
    private String name;
    CrmCustomerDateEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id;}

    public String getName() { return name; }
}
