package com.kakarote.crm9.utils;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/25 1:58 下午
 */
public class PagerUtil {
    /**
     * 默认页
     */
    public static final Integer DEFAULT_PAGE = 1;

    /**
     * 默认页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 20;

    public static Long getStart(Integer page, Integer pageSize) {
        if (page == null) {
            page = DEFAULT_PAGE;
        }
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return (long) ((page - 1) * pageSize);
    }
}
