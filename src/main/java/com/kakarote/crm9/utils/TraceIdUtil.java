package com.kakarote.crm9.utils;

import java.util.UUID;

public class TraceIdUtil {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<String>();

    public static String getTraceId() {
        if (TRACE_ID.get() == null) {
            String s = UUID.randomUUID().toString().replace("-", "");
            setTraceId(s);
        }
        return TRACE_ID.get();
    }

    private static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static void remove() {
        TRACE_ID.remove();
    }
}
