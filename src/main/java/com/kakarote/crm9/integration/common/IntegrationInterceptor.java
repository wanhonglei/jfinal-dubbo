package com.kakarote.crm9.integration.common;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * Integration interceptor.
 *
 * @author hao.fu
 * @create 2019/6/26 14:46
 */
public class IntegrationInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation inv) {
        inv.invoke();
    }
}
