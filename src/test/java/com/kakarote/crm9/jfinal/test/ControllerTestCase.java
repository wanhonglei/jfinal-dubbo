/*
 * Copyright 2018 Jobsz (zcq@zhucongqi.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kakarote.crm9.jfinal.test;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.config.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.core.Action;
import com.jfinal.core.ActionHandler;
import com.jfinal.core.ActionMapping;
import com.jfinal.core.JFinal;
import com.jfinal.handler.Handler;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.interceptor.AuthInterceptor;
import com.kakarote.crm9.common.interceptor.ErpInterceptor;
import com.kakarote.crm9.jfinal.kits.Reflect;
import io.undertow.io.Receiver;
import io.undertow.io.Sender;
import io.undertow.server.BlockingHttpExchange;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.core.ManagedServlet;
import io.undertow.servlet.handlers.ServletChain;
import io.undertow.servlet.handlers.ServletHandler;
import io.undertow.servlet.handlers.ServletPathMatch;
import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.MappingMatch;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ControllerTestCase<T extends JFinalConfig> {
    protected static final Log LOG = Log.getLog(ControllerTestCase.class);
    protected static ServletContext servletContext = new MockServletContext();
    protected static MockHttpRequest request;
    protected static MockHttpResponse response;
    protected static Handler handler;
    protected static ActionHandler actionHandler;
    private static boolean configStarted = false;
    private static JFinalConfig configInstance;
    protected static AbstractApplicationContext applicationContext;
    protected static ServletRequestContext servletRequestContext;
    private String actionUrl;
    private String bodyData;
    private File bodyFile;
    private File responseFile;
    private Class<? extends JFinalConfig> config;

    private static void initConfig(Class<JFinal> clazz, JFinal me, ServletContext servletContext, JFinalConfig config) {
        Reflect.on(me).call("init", config, servletContext);
    }

    public static void start(Class<? extends JFinalConfig> configClass) throws Exception {
        if (configStarted) {
            return;
        }
        Class<JFinal> clazz = JFinal.class;
        JFinal me = JFinal.me();
        configInstance = configClass.newInstance();
        initConfig(clazz, me, servletContext, configInstance);
        handler = Reflect.on(me).get("handler");
        actionHandler = new ActionHandler();
        ActionMapping actionMapping = Reflect.on(me).get("actionMapping");
        //去除ErpInterceptor
        for (String actionKey : actionMapping.getAllActionKeys()) {
            Action action = actionMapping.getAction(actionKey, null);
            if (action != null) {
                Reflect reflect = Reflect.on(action);
                Interceptor[] interceptors = reflect.get("interceptors");
                for (int i = 0; i < interceptors.length; i++) {
                    Interceptor interceptor = interceptors[i];
                    if (interceptor instanceof ErpInterceptor || interceptor instanceof AuthInterceptor) {
                        //替换拦截器
                        interceptors[i] = Invocation::invoke;
                    }
                }
            }
        }
        Constants constants = Reflect.on(me).get("constants");
        Reflect.on(actionHandler).call("init", actionMapping, constants);
        configStarted = true;
        configInstance.onStart();
    }

    @SuppressWarnings("unchecked")
    public ControllerTestCase() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        Preconditions.checkArgument(genericSuperclass instanceof ParameterizedType,
                "Your ControllerTestCase must have genericType");
        config = (Class<? extends JFinalConfig>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
    }

    public Object findAttrAfterInvoke(String key) {
        return request.getAttribute(key);
    }

    private String getTarget(String url, MockHttpRequest request) {
        String target = url;
        if (url.contains("?")) {
            target = url.substring(0, url.indexOf("?"));
            String queryString = url.substring(url.indexOf("?") + 1);
            String[] keyVals = queryString.split("&");
            for (String keyVal : keyVals) {
                int i = keyVal.indexOf('=');
                String key = keyVal.substring(0, i);
                String val = keyVal.substring(i + 1);
                request.setParameter(key, val);
            }
        }
        return target;

    }

    /**
     * 组装URL参数，设置METHOD(默认是GET，如需自定义自己改造一下)
     *
     * @param url
     * @param request
     * @return
     */
    private String getTarget(String url, HttpServletRequestImpl request) {
        String target = url;
        if (url.contains("?")) {
            target = url.substring(0, url.indexOf("?"));
            String queryString = url.substring(url.indexOf("?") + 1);
            Map<String, Deque<String>> queryParams = new HashMap<>();
            String[] keyVals = queryString.split("&");
            for (String keyVal : keyVals) {
                int i = keyVal.indexOf('=');
                String key = keyVal.substring(0, i);
                String val = keyVal.substring(i + 1);
                Deque<String> deque = new ArrayDeque<>();
                deque.add(val);
                queryParams.put(key, deque);
            }
            request.setQueryParameters(queryParams);
        }
        if (StringUtils.isNoneBlank(bodyData)) {
            request.getExchange().setRequestMethod(Methods.POST);
        } else {
            request.getExchange().setRequestMethod(Methods.GET);
        }
        return target;
    }

    @Before
    public void init() throws Exception {
        setupApplicationContext();
        start(config);
    }

    @AfterClass
    public static void stop() throws Exception {
        configInstance.onStop();
    }

    public void setupApplicationContext() {
        applicationContext = new GenericWebApplicationContext();
        applicationContext.refresh();
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
    }

    public String invoke(HttpServletRequestImpl request) {
        if (bodyFile != null) {
            List<String> req = null;
            try {
                req = Files.readLines(bodyFile, Charsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bodyData = Joiner.on("").join(req);
        }
        StringWriter resp = new StringWriter();

        //添加请求头
        HeaderMap headerMap = Reflect.on(request.getExchange()).field("requestHeaders").get();
        headerMap.put(Headers.HOST, actionUrl);

        //设置request的URI
        request.getExchange().setRequestURI(actionUrl);

        //post的破玩意
        HttpServletResponseImpl httpServletResponse = new HttpServletResponseImpl(request.getExchange(), request.getServletContext());
        ManagedServlet managedServlet = new ManagedServlet(new ServletInfo("MockServlet", MockServlet.class), request.getServletContext());
        ServletHandler servletHandler = new ServletHandler(managedServlet);
        ServletChain servletChain = new ServletChain(servletHandler, managedServlet, actionUrl, true, MappingMatch.CONTEXT_ROOT, actionUrl, new HashMap<>());
        ServletPathMatch servletPathMatch = new ServletPathMatch(servletChain, actionUrl, false);
        ServletRequestContext servletRequestContext = new ServletRequestContext(request.getServletContext().getDeployment(), request, httpServletResponse, servletPathMatch);
        ServletRequestContext.setCurrentRequestContext(servletRequestContext);
        servletRequestContext.setCurrentServlet(servletChain);
        request.getExchange().putAttachment(ServletRequestContext.ATTACHMENT_KEY, servletRequestContext);

        //支持getRawData
        if (StringUtils.isNoneBlank(bodyData)) {
            Reflect.on(request.getExchange()).set("blockingHttpExchange", new BlockingHttpExchange() {
                @Override
                public InputStream getInputStream() {
                    final byte[] bytes = bodyData.getBytes();
                    return new InputStream() {
                        int idx = 0;

                        @Override
                        public int read() {
                            return idx < bytes.length ? bytes[idx++] : -1;
                        }
                    };
                }

                @Override
                public OutputStream getOutputStream() {
                    return null;
                }

                @Override
                public Sender getSender() {
                    return null;
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public Receiver getReceiver() {
                    return null;
                }
            });
        }
        response = new MockHttpResponse(resp);
        //执行
        actionHandler.handle(getTarget(actionUrl, request), request, response, new boolean[]{true});
//        Reflect.on(handler).call("handle", getTarget(actionUrl, request), request, response, new boolean[] { true });
        //返回response
        String response = resp.toString();
        if (responseFile != null) {
            try {
                Files.asCharSink(responseFile, Charsets.UTF_8).write(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return response;
    }

    public ControllerTestCase<T> post(File bodyFile) {
        this.bodyFile = bodyFile;
        return this;
    }

    public ControllerTestCase<T> post(String bodyData) {
        this.bodyData = bodyData;
        return this;
    }

    public ControllerTestCase<T> use(String actionUrl) {
        this.actionUrl = actionUrl;
        return this;
    }

    public ControllerTestCase<T> writeTo(File responseFile) {
        this.responseFile = responseFile;
        return this;
    }

}
