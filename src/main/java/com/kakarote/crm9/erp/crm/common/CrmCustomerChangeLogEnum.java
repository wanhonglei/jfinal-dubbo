package com.kakarote.crm9.erp.crm.common;

/**
 * @Author: honglei.wan
 * @Description:客户变化枚举类
 * @Date: Create in 2020/4/20 5:43 下午
 */
public enum CrmCustomerChangeLogEnum {

    /**
     * 客户变化枚举类
     */
    OPEN_SEA(0,"公海"),
    DEPT(1,"部门"),
    INSPECT_BD(2,"考察库BD"),
    RELATE_BD(3,"关联库BD"),
    ;

    private int code;
    private String msg;

    CrmCustomerChangeLogEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static int getBdByStorageType(Integer storageType){
        if (storageType == null){
            //默认考察库BD
            return CrmCustomerChangeLogEnum.INSPECT_BD.code;
        }else if (storageType == CustomerStorageTypeEnum.INSPECT_CAP.getCode()){
            return CrmCustomerChangeLogEnum.INSPECT_BD.code;
        }else if (storageType == CustomerStorageTypeEnum.RELATE_CAP.getCode()){
            return CrmCustomerChangeLogEnum.RELATE_BD.code;
        }else {
            //默认考察库BD
            return CrmCustomerChangeLogEnum.INSPECT_BD.code;
        }
    }
}
