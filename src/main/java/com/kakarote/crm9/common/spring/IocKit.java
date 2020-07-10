package com.kakarote.crm9.common.spring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.util.Assert;

import com.jfinal.config.JFinalConfig;
import com.jfinal.core.Controller;
import com.jfinal.core.JFinal;

/**
 * IocKit
 *
 * @author hao.fu
 * @create 2019/7/4 11:52
 */
public class IocKit {

    /**
     * ioc @Inject @Value @Autowired
     */
    static AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor;

    /**
     * ioc @Resource
     */
    static CommonAnnotationBeanPostProcessor commonAnnotationBeanPostProcessor;

    public static AutowiredAnnotationBeanPostProcessor getAutowiredAnnotationBeanPostProcessor() {
        Assert.notNull(autowiredAnnotationBeanPostProcessor,
                "The main autowiredAnnotationBeanPostProcessor is null, initialize SpringPlugin first");
        return autowiredAnnotationBeanPostProcessor;
    }

    public static CommonAnnotationBeanPostProcessor getCommonAnnotationBeanPostProcessor() {
        Assert.notNull(commonAnnotationBeanPostProcessor,
                "The main commonAnnotationBeanPostProcessor is null, initialize SpringPlugin first");
        return commonAnnotationBeanPostProcessor;
    }

    // --------------------------------------------------
    // JFinalConfig
    // --------------------------------------------------

    /**
     * 处理 JFinalConfig
     */
    public static void processJFinalConfig(JFinalConfig finalConfig) {
        ApplicationContext ctx = org.springframework.web.context.support.WebApplicationContextUtils
                .getWebApplicationContext(JFinal.me().getServletContext());
        processJFinalConfig(finalConfig, ctx);
    }

    /**
     * 处理 JFinalConfig
     */
    public static void processJFinalConfig(JFinalConfig jfinalConfig, ApplicationContext ctx) {
        Assert.notNull(ctx, "ApplicationContext not be null");
        Assert.notNull(jfinalConfig, "jfinalConfig not be null");
        AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = setBeanFactory(new AutowiredAnnotationBeanPostProcessor(), ctx);
        CommonAnnotationBeanPostProcessor commonAnnotationBeanPostProcessor = setBeanFactory(new CommonAnnotationBeanPostProcessor(), ctx);

        processInjection(jfinalConfig, commonAnnotationBeanPostProcessor, autowiredAnnotationBeanPostProcessor);
    }

    /**
     * 注入 bean 调用
     */
    public static void invokeForProcessInjection(Object invocation) {
        Invocation inv = as(invocation, Invocation.class);
        Controller controller = inv.getController();
        processInjection(controller);
        inv.invoke();
    }

    public static void processInjection(Object bean) {
        processInjection(bean, commonAnnotationBeanPostProcessor, autowiredAnnotationBeanPostProcessor);
    }

    // ---------------------------------------------------------
    // function
    // ---------------------------------------------------------

    static void init(ApplicationContext ctx) {
        autowiredAnnotationBeanPostProcessor = setBeanFactory(new AutowiredAnnotationBeanPostProcessor(), ctx);
        commonAnnotationBeanPostProcessor = setBeanFactory(new CommonAnnotationBeanPostProcessor(), ctx);
    }

    static <T extends BeanFactoryAware> T setBeanFactory(T beanPostProcessor, ApplicationContext ctx) {
        beanPostProcessor.setBeanFactory(ctx.getAutowireCapableBeanFactory());
        return beanPostProcessor;
    }

    static void processInjection(Object bean, CommonAnnotationBeanPostProcessor commonAnnotationBeanPostProcessor,
                                 AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor) {
        commonAnnotationBeanPostProcessor.postProcessPropertyValues(null, null, bean, null);
        autowiredAnnotationBeanPostProcessor.processInjection(bean);
    }

    // ----------------------------------------------------------------
    // as proxy
    // ----------------------------------------------------------------

    private final static Map<String, Method> METHOD_CACHE = new HashMap<>(16);

    private static Method cachedMethod(Class<?> type, Method method) throws NoSuchMethodException {
        String name = method.getName();
        String key = type.getName() + "." + name;

        Method m = METHOD_CACHE.get(key);

        if (m == null) {
            synchronized (METHOD_CACHE) {
                m = METHOD_CACHE.get(key);
                if (m == null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    try {
                        // Actual method name matches always come first
                        m = type.getMethod(name, parameterTypes);
                    } catch (SecurityException ignore) {
                        m = type.getDeclaredMethod(name, parameterTypes);
                    }

                    METHOD_CACHE.put(key, m);
                }
            }
        }
        return m;
    }

    /**
     * Create a proxy for the wrapped object allowing to typesafely invoke
     * methods on it using a custom interface
     *
     * @param proxyType The interface type that is implemented by the proxy
     * @return A proxy for the wrapped object
     */
    @SuppressWarnings("unchecked")
    private static <P> P as(final Object object, Class<P> proxyType) {
        final Class<?> clazz = object.getClass();

        final InvocationHandler handler = (proxy, method, args) -> {
            Method m = cachedMethod(clazz, method);
            boolean accessible = m.isAccessible();
            try {
                if (!accessible) {
                    m.setAccessible(true);
                }
                return m.invoke(object, args);
            } finally {
                m.setAccessible(accessible);
            }
        };

        return (P) Proxy.newProxyInstance(proxyType.getClassLoader(), new Class[]{proxyType}, handler);
    }

    private interface Invocation {
        void invoke();

        Controller getController();
    }
}
