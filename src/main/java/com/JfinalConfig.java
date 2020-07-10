package com;

import com.jfinal.aop.AopManager;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.app.config.MyAopFactory;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.rpc.RpcManager;
import com.jfinal.template.Engine;
import com.jfinalRouter;
import com.log.LogBackLogFactory;

/**
 * API 引导式配置
 * @author lcc
 */
public class JfinalConfig extends JFinalConfig {

    public JfinalConfig(){
        AopManager.me().setAopFactory(MyAopFactory.me());
    }

    /**
     * 配置常量
     */
    @Override
    public void configConstant(Constants me) {
        // 配置依赖注入
        me.setInjectDependency(true);
        // 父类注入
        me.setInjectSuperClass(true);
        //限制上传100M
        me.setMaxPostSize(104857600);
        // 配置开发模式，true 值为开发模式
        me.setDevMode(Boolean.TRUE);
        //配置logback
        me.setLogFactory(new LogBackLogFactory());
    }

    /**
     * 配置路由
     */
    @Override
    public void configRoute(Routes me) {
        me.add(new jfinalRouter());
    }

    @Override
    public void configEngine(Engine me) {

    	// Do nothing
    }

    /**
     * 配置插件
     */
    @Override
    public void configPlugin(Plugins me) {
        //cron定时器
        me.add(new Cron4jPlugin(PropKit.use("config/cron4j.txt")));
    }

    /**
     * 配置全局拦截器
     */
    @Override
    public void configInterceptor(Interceptors me) {

    }

    /**
     * 配置处理器
     */
    @Override
    public void configHandler(Handlers me) {

    }

    @Override
    public void onStart() {
        RpcManager.me().init();
    }

    @Override
    public void onStop() {
        RpcManager.me().stop();
    }

}
