package com.kakarote.crm9.common.spring;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.IPlugin;

import java.util.Arrays;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.util.Assert;

/**
 * Spring Plugin
 *
 * @author hao.fu
 * @create 2019/7/3 15:07
 */
public class SpringPlugin implements IPlugin {

    private boolean isStarted = false;
    private String[] configurations;
    private ApplicationContext ctx;

    public SpringPlugin() {
    }

    public SpringPlugin(ApplicationContext ctx) {
        this.setApplicationContext(ctx);
    }

    public SpringPlugin(String... configurations) {
    	super();
        if (null!=configurations) {
        	this.configurations = Arrays.copyOf(configurations, configurations.length);
        } else {
        	this.configurations = new String[0];
        }
    }

    public final void setApplicationContext(ApplicationContext ctx) {
        Assert.notNull(ctx, "ApplicationContext can not be null.");
        this.ctx = ctx;
    }

    public ApplicationContext getApplicationContext() {
        return this.ctx;
    }

    @Override
    public boolean start() {
        if (isStarted) {
//            return true;
        } else if (configurations != null) {
            ctx = new ClassPathXmlApplicationContext(configurations);
            IocKit.init(ctx);
        } else {
            ctx = new FileSystemXmlApplicationContext(PathKit.getWebRootPath() + "/WEB-INF/applicationContext.xml");
            IocKit.init(ctx);
        }
        return isStarted = true;
    }

    @Override
    public boolean stop() {
        ctx = null;
        isStarted = false;
        return true;
    }

    public ApplicationContext getCtx() {
        return ctx;
    }
}
