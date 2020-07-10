package com.kakarote.crm9.utils;

import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/15 10:06 上午
 */
public class StrUtilExt extends StrUtil {
    public static String join(CharSequence delimiter, List<Long> elements) {
        if (elements == null || elements.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            sb.append(elements.get(i));
            if (i < elements.size() - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }
}
