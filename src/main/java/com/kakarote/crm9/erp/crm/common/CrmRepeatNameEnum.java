package com.kakarote.crm9.erp.crm.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/4/24 2:59 下午
 */
public enum CrmRepeatNameEnum {
    /**
     * CrmRepeatNameEnum
     */
    BRANCH_OFFICE("分公司"),
    BRACE(")"),
    ;

    private String name;
    CrmRepeatNameEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getAll(){
        List<String> allList = new ArrayList<>();
        CrmRepeatNameEnum[] enums = CrmRepeatNameEnum.values();
        for (CrmRepeatNameEnum nameEnum : enums){
            allList.add(nameEnum.name);
        }
        return allList;
    }

}
