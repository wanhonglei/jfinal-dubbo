package com.kakarote.crm9.common.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.aop.AopManager;
import com.jfinal.config.*;
import com.jfinal.core.paragetter.ParaProcessorBuilder;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.app.config.MyAopFactory;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.plugin.rpc.RpcManager;
import com.jfinal.template.Engine;
import com.kakarote.crm9.Application;
import com.kakarote.crm9.common.config.cache.CaffeineCache;
import com.kakarote.crm9.common.config.cache.MyJdkSerializer;
import com.kakarote.crm9.common.config.json.ErpJsonFactory;
import com.kakarote.crm9.common.config.log.LogBackLogFactory;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.config.paragetter.PageParaGetter;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.interceptor.ErpInterceptor;
import com.kakarote.crm9.common.spring.IocKit;
import com.kakarote.crm9.common.spring.SpringPlugin;
import com.kakarote.crm9.erp.MappingKit;
import com.kakarote.crm9.erp.admin.common.AdminRouter;
import com.kakarote.crm9.erp.crm.common.CrmDirective;
import com.kakarote.crm9.erp.crm.common.CrmRouter;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.integration.common.IntegrationRouter;
import com.kakarote.crm9.mobile.common.MobileRouter;
import com.kakarote.crm9.utils.CrmProps;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * API 引导式配置
 * @author lcc
 */
public class JfinalConfig extends JFinalConfig {

    private static Log logger = Log.getLog(Application.class);

    public static final CrmProps crmProp = CrmProps.getInstance();

    public JfinalConfig(){
        AopManager.me().setAopFactory(MyAopFactory.me());
    }

    /**
     * 配置常量
     */
    @Override
    public void configConstant(Constants me) {
        //spring 支持
        IocKit.processJFinalConfig(this);
        // 配置依赖注入
        me.setInjectDependency(true);
        // 父类注入
        me.setInjectSuperClass(true);
        //上传路径
        me.setBaseUploadPath(BaseConstant.UPLOAD_PATH);
        //下载路径
        me.setBaseDownloadPath(BaseConstant.UPLOAD_PATH);
        // 设置 Json 转换工厂实现类
        me.setJsonFactory(new ErpJsonFactory());
        //限制上传100M
        me.setMaxPostSize(104857600);
        // 配置开发模式，true 值为开发模式
        me.setDevMode(crmProp.getBoolean("jfinal.devMode", Boolean.TRUE));
        //配置logback
        me.setLogFactory(new LogBackLogFactory());
    }

    /**
     * 配置路由
     */
    @Override
    public void configRoute(Routes me) {
        me.add(new AdminRouter());
        me.add(new CrmRouter());
        me.add(new IntegrationRouter());
        me.add(new MobileRouter());
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
        ParaProcessorBuilder.me.regist(BasePageRequest.class, PageParaGetter.class, null);
        // 配置 druid 数据库连接池插件
        DruidPlugin druidPlugin = createDruidPlugin();
        me.add(druidPlugin);
        // 配置ActiveRecord插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setCache(CaffeineCache.ME);
        arp.setDialect(new MysqlDialect());
        arp.getEngine().addDirective("CrmAuth", CrmDirective.class);
        arp.setShowSql(crmProp.getBoolean("sql.show"));
        me.add(arp);
        //扫描sql模板
        getSqlTemplate(PathKit.getRootClassPath() + "/template", arp);
        //Redis以及缓存插件
        createRedisPlugin(me);
        //model映射
        MappingKit.mapping(arp);
        //spring plugin
        me.add(new SpringPlugin("classpath*:config/crmApplicationContext.xml"));
        //cron定时器
        me.add(new Cron4jPlugin(PropKit.use("config/cron4j.txt")));
    }

    /**
     * 配置全局拦截器
     */
    @Override
    public void configInterceptor(Interceptors me) {
        //添加全局拦截器
        me.addGlobalActionInterceptor(new ErpInterceptor());
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

    /**
     * 加在sql模版
     * @param path
     * @param arp
     */
    public static void getSqlTemplate(String path, ActiveRecordPlugin arp) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File childFile : files) {
                    if (childFile.isDirectory()) {
                        getSqlTemplate(childFile.getAbsolutePath(), arp);
                    } else {
                        if (childFile.getName().toLowerCase().endsWith(".sql")) {
                            arp.addSqlTemplate(childFile.getAbsolutePath().replace(PathKit.getRootClassPath(), "").replace('\\', '/'));
                        }
                    }
                }
            }
        }
    }

    public static DruidPlugin createDruidPlugin() {
        DruidPlugin druidPlugin = new DruidPlugin(getJdbcUrl().trim(), getDbUser().trim(), getDbPassword().trim()).setInitialSize(1).setMinIdle(1).setMaxActive(2000).setTimeBetweenEvictionRunsMillis(5000).setValidationQuery("select 1").setTimeBetweenEvictionRunsMillis(60000).setMinEvictableIdleTimeMillis(30000).setFilters("stat,wall");
        druidPlugin.addFilter(new WallFilter());
        druidPlugin.setInitialSize(0);
        druidPlugin.setMinIdle(0);
        druidPlugin.setMaxActive(2000);
        druidPlugin.setTimeBetweenEvictionRunsMillis(5000);
        druidPlugin.setValidationQuery("select 1");
        druidPlugin.setTimeBetweenEvictionRunsMillis(60000);
        druidPlugin.setMinEvictableIdleTimeMillis(30000);
        return druidPlugin;
    }

    private static String getJdbcUrl() {
        String url = StringUtils.isNotEmpty(crmProp.get(CrmConstant.SR_DB_HOST)) ? buildSrJdbcUrl() : crmProp.get(CrmConstant.COLUMBUS_DB_MYSQL_JDBC_URL_KEY);
        logger.info(String.format("#####################JDBC URL: %s", url));
        return url;
    }

    private static String buildSrJdbcUrl() {
        return "jdbc:mysql://" + crmProp.get(CrmConstant.SR_DB_HOST) + ":" + crmProp.get(CrmConstant.SR_DB_PORT) + "/" + crmProp.get(CrmConstant.SR_DB_DATABASE_KEY) + "?characterEncoding=utf8&useSSL=false&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false";
    }

    private static String getDbUser() {
        String user = StringUtils.isNotEmpty(crmProp.get(CrmConstant.SR_DB_ACCOUNT_KEY)) ? crmProp.get(CrmConstant.SR_DB_ACCOUNT_KEY) : crmProp.get(CrmConstant.COLUMBUS_DB_MYSQL_USER_KEY);
        logger.info(String.format("#####################JDBC USER: %s", user));
        return user;
    }

    private static String getDbPassword() {
        String password = StringUtils.isNotEmpty(crmProp.get(CrmConstant.SR_DB_ACCOUNT_SECRET_KEY)) ? crmProp.get(CrmConstant.SR_DB_ACCOUNT_SECRET_KEY) : crmProp.get(CrmConstant.COLUMBUS_DB_MYSQL_PASSWORD_KEY);
        logger.info(String.format("#####################JDBC PASSWORD: %s", password));
        return password;
    }

    private void createRedisPlugin(Plugins me) {
        for (String configName : crmProp.get("jfinal.redis", "").split(",")) {
            RedisPlugin redisPlugin;
            if (crmProp.getBoolean(configName + ".open", Boolean.FALSE)) {
                if (crmProp.containsKey(configName + ".password") && StrUtil.isNotEmpty(crmProp.get(configName + ".password"))) {
                    redisPlugin = new RedisPlugin(crmProp.get(configName + ".cacheName").trim(), crmProp.get(configName + ".host").trim(), crmProp.getInt(configName + ".port", 6379), crmProp.getInt(configName + ".timeout", 20000), crmProp.get(configName + ".password", null),crmProp.getInt(configName+".database",0));
                } else {
                    redisPlugin = new RedisPlugin(crmProp.get(configName + ".cacheName").trim(), crmProp.get(configName + ".host").trim(), crmProp.getInt(configName + ".port", 6379), crmProp.getInt(configName + ".timeout", 20000));
                }
                redisPlugin.setSerializer(MyJdkSerializer.MY_JDK_SERIALIZER);
                me.add(redisPlugin);
            }
        }
    }

}
