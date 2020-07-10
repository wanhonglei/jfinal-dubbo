package com.kakarote.crm9.common.interceptor;

import cn.hutool.core.date.DateTime;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.render.JsonRender;
import com.jfinal.render.Render;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.utils.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * @author liming.guo
 */
@Slf4j
public class LogApiOperationInterceptor implements Interceptor {


    @Override
    public void intercept(Invocation inv) {
        //aop拦截打印请求日志
        Controller controller = inv.getController();
        Method method = inv.getMethod();
        LogApiOperation logApiOperation = method.getAnnotation(LogApiOperation.class);
        long startTime = System.currentTimeMillis();
        String requestTimeStr = new DateTime().toMsStr();
        boolean printLog = logApiOperation != null;
        String traceId = TraceIdUtil.getTraceId();
        try {
            inv.invoke();
            if (printLog) {
                try {
                    long time = System.currentTimeMillis() - startTime;
                    String responseTimeStr = new DateTime().toMsStr();
                    Render render = controller.getRender();
                    String responseJson = (render == null ? "" : ((JsonRender) render).getJsonText());
                    String methodName = StringUtils.isBlank(logApiOperation.methodName()) ? method.getName() : logApiOperation.methodName();
                    log.info("traceId:{},请求时间:{},响应返回时间:{},请求地址:{},请求方法:{},Body:{},Params:{},返回参数:{},响应处理时间:{}",
                            traceId, requestTimeStr, responseTimeStr, controller.getRequest().getRequestURL(), methodName,
                            controller.getRawData(), JsonKit.toJson(controller.getParaMap()), responseJson, time);
                } catch (Exception e) {
                    log.warn("LogApiOperationInterceptor intercept exception:{}", e.getMessage());
                }
            }
        } finally {
            TraceIdUtil.remove();
        }
    }
}
