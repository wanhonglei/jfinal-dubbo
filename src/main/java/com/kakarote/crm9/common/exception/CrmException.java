package com.kakarote.crm9.common.exception;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/11 3:15 下午
 */
public class CrmException extends RuntimeException {

    public CrmException(String message) {
        super(message);
    }

    public CrmException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrmException(Throwable cause) {
        super(cause);
    }

    /**
     * 传入异常，只输出
     * @param e
     */
    public CrmException(Exception e) {
        super(e.getMessage());
    }

    protected CrmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
