package com.kakarote.crm9.common.spring;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * Spring IocInterceptor
 *
 * @author hao.fu
 * @create 2019/7/3 15:08
 */
public class IocInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        IocKit.invokeForProcessInjection(inv);
    }
}
