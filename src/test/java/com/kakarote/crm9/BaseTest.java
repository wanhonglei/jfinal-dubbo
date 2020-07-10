package com.kakarote.crm9;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.aop.Aop;
import com.jfinal.aop.AopManager;
import com.jfinal.aop.Inject;
import com.jfinal.json.JsonManager;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.jfinal.template.source.FileSourceFactory;
import com.kakarote.crm9.common.config.cache.CaffeineCache;
import com.kakarote.crm9.common.config.cache.MyJdkSerializer;
import com.kakarote.crm9.common.config.json.ErpJsonFactory;
import com.kakarote.crm9.erp.MappingKit;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmDirective;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmProps;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.qxwz.lyra.common.oss.dal.impl.OSSDaoImpl;
import io.undertow.UndertowMessages;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.*;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.core.*;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import io.undertow.servlet.spec.ServletContextImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.xnio.*;
import org.xnio.channels.ConnectedChannel;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;
import org.xnio.conduits.StreamSinkConduit;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BaseTest
 *
 * @author hao.fu
 * @since 2019/6/27 18:50
 */
public class BaseTest {

    private static Log logger = Log.getLog(BaseTest.class);

    @Inject
    private AdminUserService adminUserService = Aop.get(AdminUserService.class);

    @Inject
    private AdminRoleService adminRoleService = Aop.get(AdminRoleService.class);

    private static ActiveRecordPlugin activeRecord;
    private static RedisPlugin redisPlugin;

    @Before
    public void setUp() {
        AopManager.me().setInjectSuperClass(true);

        JsonManager.me().setDefaultJsonFactory(new ErpJsonFactory());

        CrmProps props = CrmProps.getInstance();
        DruidPlugin druidPlugin = createDruidPlugin(props);
        activeRecord = new ActiveRecordPlugin(druidPlugin);
        activeRecord.setCache(CaffeineCache.ME);
        activeRecord.setDevMode(true);
        activeRecord.setShowSql(true);
        activeRecord.setDialect(new MysqlDialect());

        Engine engine = activeRecord.getEngine();


        engine.addDirective("CrmAuth", CrmDirective.class);
        engine.setSourceFactory(new FileSourceFactory());
        engine.setBaseTemplatePath(null);

        getSqlTemplateForPC(PathKit.getWebRootPath() + "/src/main/resources/template", activeRecord);

        //maven执行的时候，需要从classpath下获取sql模版
        activeRecord.getSqlKit().parseSqlTemplate();
        if (activeRecord.getSqlKit().getSqlMapEntrySet().size() == 0) {
            engine.setSourceFactory(new ClassPathSourceFactory());
            getSqlTemplateForMaven(PathKit.getRootClassPath() + "/template", activeRecord);

        }

        MappingKit.mapping(activeRecord);

        createRedisPin(props);

        druidPlugin.start();
        activeRecord.start();
        redisPlugin.start();

        mockBaseUtil("0946");
    }

    private void getSqlTemplateForMaven(String path, ActiveRecordPlugin arp) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File childFile : files) {
                    if (childFile.isDirectory()) {
                        getSqlTemplateForMaven(childFile.getAbsolutePath(), arp);
                    } else {
                        if (childFile.getName().toLowerCase().endsWith(".sql")) {
                            arp.addSqlTemplate(childFile.getAbsolutePath().replace(PathKit.getRootClassPath(), "").replace('\\', '/'));
                        }
                    }
                }
            }
        }
    }

    private void getSqlTemplateForPC(String path, ActiveRecordPlugin arp) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File childFile : files) {
                    if (childFile.isDirectory()) {
                        getSqlTemplateForPC(childFile.getAbsolutePath(), arp);
                    } else {
                        if (childFile.getName().toLowerCase().endsWith(".sql")) {
                            arp.addSqlTemplate("/" + childFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    @After
    public void tearDown() {
        activeRecord.stop();
        redisPlugin.stop();
    }

    private void createRedisPin(CrmProps crmProp) {
        for (String configName : crmProp.get("jfinal.redis", "").split(",")) {
            if (crmProp.getBoolean(configName + ".open", Boolean.FALSE)) {
                if (crmProp.containsKey(configName + ".password") && StrUtil.isNotEmpty(crmProp.get(configName + ".password"))) {
                    redisPlugin = new RedisPlugin(crmProp.get(configName + ".cacheName").trim(), crmProp.get(configName + ".host").trim(), crmProp.getInt(configName + ".port", 6379), crmProp.getInt(configName + ".timeout", 20000), crmProp.get(configName + ".password", null), crmProp.getInt(configName + ".database", 0));
                } else {
                    redisPlugin = new RedisPlugin(crmProp.get(configName + ".cacheName").trim(), crmProp.get(configName + ".host").trim(), crmProp.getInt(configName + ".port", 6379), crmProp.getInt(configName + ".timeout", 20000));
                }

                redisPlugin.setSerializer(MyJdkSerializer.MY_JDK_SERIALIZER);

            }
        }
    }

    public DruidPlugin createDruidPlugin(CrmProps prop) {

//        DruidPlugin druidPlugin = new DruidPlugin(prop.get("mysql.jdbcUrl"), prop.get("mysql.user"), prop.get("mysql.password").trim()).setInitialSize(1).setMinIdle(1).setMaxActive(2000).setTimeBetweenEvictionRunsMillis(5000).setValidationQuery("select 1").setTimeBetweenEvictionRunsMillis(60000).setMinEvictableIdleTimeMillis(30000).setFilters("stat,wall");
        DruidPlugin druidPlugin = new DruidPlugin(getJdbcUrl(prop).trim(), getDbUser(prop).trim(), getDbPassword(prop).trim()).setInitialSize(1).setMinIdle(1).setMaxActive(2000).setTimeBetweenEvictionRunsMillis(5000).setValidationQuery("select 1").setTimeBetweenEvictionRunsMillis(60000).setMinEvictableIdleTimeMillis(30000).setFilters("stat,wall");
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

    private static String getDbUser(CrmProps crmProp) {
        String user = StringUtils.isNotEmpty(crmProp.get(CrmConstant.SR_DB_ACCOUNT_KEY)) ? crmProp.get(CrmConstant.SR_DB_ACCOUNT_KEY) : crmProp.get(CrmConstant.COLUMBUS_DB_MYSQL_USER_KEY);
        logger.info(String.format("#####################JDBC USER: %s", user));
        return user;
    }

    private static String getJdbcUrl(CrmProps crmProp) {
        String url = StringUtils.isNotEmpty(crmProp.get(CrmConstant.SR_DB_HOST)) ? buildSrJdbcUrl(crmProp) : crmProp.get(CrmConstant.COLUMBUS_DB_MYSQL_JDBC_URL_KEY);
        logger.info(String.format("#####################JDBC URL: %s", url));
        return url;
    }

    private static String getDbPassword(CrmProps crmProp) {
        String password = StringUtils.isNotEmpty(crmProp.get(CrmConstant.SR_DB_ACCOUNT_SECRET_KEY)) ? crmProp.get(CrmConstant.SR_DB_ACCOUNT_SECRET_KEY) : crmProp.get(CrmConstant.COLUMBUS_DB_MYSQL_PASSWORD_KEY);
        logger.info(String.format("#####################JDBC PASSWORD: %s", password));
        return password;
    }

    private static String buildSrJdbcUrl(CrmProps crmProp) {
        return "jdbc:mysql://" + crmProp.get(CrmConstant.SR_DB_HOST) + ":" + crmProp.get(CrmConstant.SR_DB_PORT) + "/" + crmProp.get(CrmConstant.SR_DB_DATABASE_KEY) + "?characterEncoding=utf8&useSSL=false&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false";
    }

    public EsbConfig mockEsbConfig() {
        try {
            CrmProps props = CrmProps.getInstance();
            EsbConfig esbConfig = new EsbConfig();
            injectField(EsbConfig.class, esbConfig, "deptSyncUrl", props.get("dept.sync.url"));
            injectField(EsbConfig.class, esbConfig, "staffSyncUrl", props.get("staff.sync.url"));
            injectField(EsbConfig.class, esbConfig, "esbOaUser", props.get("esb.oa.user"));
            injectField(EsbConfig.class, esbConfig, "esbOaPassword", props.get("esb.oa.password"));
            injectField(EsbConfig.class, esbConfig, "esbHost", props.get("esb.host"));
            injectField(EsbConfig.class, esbConfig, "esbPort", props.get("esb.port"));
            injectField(EsbConfig.class, esbConfig, "esbBopsPort", props.get("esb.bops.port"));
            injectField(EsbConfig.class, esbConfig, "crmEsbClientId", props.get("crm.esb.client.id"));
            injectField(EsbConfig.class, esbConfig, "opCodeSyncDept", props.get("op.code.sync.dept"));
            injectField(EsbConfig.class, esbConfig, "opCodeSyncStaff", props.get("op.code.sync.staff"));
            injectField(EsbConfig.class, esbConfig, "opCodeTagDetails", props.get("op.code.tag.details"));
            injectField(EsbConfig.class, esbConfig, "opCodeProductList", props.get("op.code.product.list"));
            injectField(EsbConfig.class, esbConfig, "tagDetailsUrl", props.get("tag.details.url"));
            injectField(EsbConfig.class, esbConfig, "esbBopsUser", props.get("esb.bops.user"));
            injectField(EsbConfig.class, esbConfig, "esbBopsPassword", props.get("esb.bops.password"));
            injectField(EsbConfig.class, esbConfig, "productListUrl", props.get("product.list.url"));
            injectField(EsbConfig.class, esbConfig, "opCodePaymentList", props.get("op.code.bops.payment.list"));
            injectField(EsbConfig.class, esbConfig, "paymentListUrl", props.get("bops.payment.list.url"));
            injectField(EsbConfig.class, esbConfig, "opCodePaymentNoList", props.get("op.code.bops.payment.nos.list"));
            injectField(EsbConfig.class, esbConfig, "paymentNoListUrl", props.get("bops.payment.nos.list.url"));
            injectField(EsbConfig.class, esbConfig, "opCodeCrmWorkflow", props.get("op.code.process.new"));
            injectField(EsbConfig.class, esbConfig, "createCrmWorkflowUrl", props.get("oa.process.new.url"));
            injectField(EsbConfig.class, esbConfig, "amapDeGeoUrl", props.get("amap.degeo.url"));
            injectField(EsbConfig.class, esbConfig, "opCodeAmapDegeo", props.get("op.code.amap.degeo"));
            injectField(EsbConfig.class, esbConfig, "amapAroundPoiUrl", props.get("amap.around.poi.url"));
            injectField(EsbConfig.class, esbConfig, "bopsCustomerOrderListUrl", props.get("backend.order.pageList.url"));
            injectField(EsbConfig.class, esbConfig, "bopsCustomerOrderListCode", props.get("backend.order.pageList.code"));
            injectField(EsbConfig.class, esbConfig, "bopsCustomerOrderClientId", props.get("backend.order.clientId"));
            injectField(EsbConfig.class, esbConfig, "opCodeAmapAroundPoi", props.get("op.code.amap.around.poi"));
            injectField(EsbConfig.class, esbConfig, "esbAmapUser", props.get("esb.amap.user"));
            injectField(EsbConfig.class, esbConfig, "esbAmapPassword", props.get("esb.amap.password"));
            return esbConfig;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mockup a CrmUser object.
     *
     * @param staffNo staff number
     * @return {@code CrmUser}
     */
    public CrmUser mockCrmUser(String staffNo) {
        AdminUser adminUser = adminUserService.getUserByStaffNo(staffNo);
        List<AdminRole> roleList = adminRoleService.getRoleByUserId(adminUser.getUserId().intValue());
        adminUser.setRoles(roleList.stream().map(item -> item.getRoleId().intValue()).collect(Collectors.toList()));
        CrmUser crmUser = new CrmUser(adminUser, roleList);
        return crmUser;
    }

    /**
     * 模拟AdminUser对象
     *
     * @param staffNo
     * @return
     */
    public AdminUser mockAdminUser(String staffNo) {
        return adminUserService.getUserByStaffNo(staffNo);
    }

    public OssPrivateFileUtil mockOssUtil() {
        CrmProps props = CrmProps.getInstance();
        OSSDaoImpl ossDao = new OSSDaoImpl();
        ossDao.setDomain(props.get("lyra.oss.domain"));
        ossDao.setApiKey(props.get("lyra.oss.api.key"));
        ossDao.setSecretKey(props.get("lyra.oss.secret.key"));
        ossDao.init();
        return new OssPrivateFileUtil(props.get("oss.private.bucket.name"), ossDao);
    }

    public void injectField(Class<?> clz, Object obj, String fieldName, Object fieldValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = clz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, fieldValue);
    }

    /**
     * Mock Request
     *
     * @param headers
     * @param setupBaseUtil 是否设置当前request到BaseUtil中
     * @return
     */
    public HttpServletRequest mockRequest(Map<String, String> headers, boolean setupBaseUtil) {
        try {
            final DefaultByteBufferPool bufferPool = new DefaultByteBufferPool(false, 1024, 0, 0);
            ServerConnection connection = new MockServerConnection(bufferPool);
            HeaderMap requestMap = new HeaderMap();
            if (Objects.nonNull(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestMap.put(HttpString.tryFromString(entry.getKey()), entry.getValue());
                }
            }
            HeaderMap responseMap = new HeaderMap();
            HttpServerExchange exchange = new HttpServerExchange(connection, requestMap, responseMap, 100L);
            ServletContainer servletContainer = new ServletContainerImpl();
            DeploymentInfo deploymentInfo = new DeploymentInfo();
            DeploymentManager deploymentManager = new DeploymentManagerImpl(deploymentInfo, servletContainer);
            DeploymentImpl deployment = new DeploymentImpl(deploymentManager, deploymentInfo, servletContainer);
            ApplicationListeners applicationListeners = new ApplicationListeners(Collections.singletonList(new ManagedListener(new ListenerInfo(MockEventListener.class), false)), new ServletContextImpl(servletContainer, deployment));
            Method method = DeploymentImpl.class.getDeclaredMethod("setApplicationListeners", ApplicationListeners.class);
            method.setAccessible(true);
            method.invoke(deployment, applicationListeners);
            ServletContextImpl serverContext = new ServletContextImpl(servletContainer, deployment);
            HttpServletRequestImpl httpServletRequest = new HttpServletRequestImpl(exchange, serverContext);
            if (setupBaseUtil) {
                BaseUtil.setRequest(httpServletRequest);
            }
            return httpServletRequest;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 模拟BaseUtil对象
     *
     * @param staffNo
     */
    public void mockBaseUtil(String staffNo) {
        CrmUser crmUser = mockCrmUser(staffNo);
        if (Objects.nonNull(crmUser)) {
            String bucToken = "CRM:UNITTEST:MOCK:BASEUTIL_" + staffNo;
            Redis.use().set(BaseUtil.LOGIN_CACHE + bucToken, crmUser);
            Redis.use().expire(BaseUtil.LOGIN_CACHE + bucToken, 300);
            Map<String, String> headers = new HashMap<>();
            headers.put(CrmConstant.BUC_AUTH_TOKEN_KEY, bucToken);
            mockRequest(headers, true);
        }
    }

    public HttpServletRequest mockRequestWithUser(String staffNo) {
        CrmUser crmUser = mockCrmUser(staffNo);
        if (Objects.nonNull(crmUser)) {
            String bucToken = "CRM:UNITTEST:MOCK:BASEUTIL_" + staffNo;
            Redis.use().set(bucToken, crmUser);
            Redis.use().expire(bucToken, 300);
            Map<String, String> headers = new HashMap<>();
            headers.put(CrmConstant.BUC_AUTH_TOKEN_KEY, bucToken);
            return mockRequest(headers, false);
        }
        return null;
    }

    public <T> T invokeMethod(Class<?> clz, Object target, String methodName, Class<?>[] paramsClz, Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clz.getDeclaredMethod(methodName, paramsClz);
        method.setAccessible(true);
        return (T) method.invoke(target, params);
    }

    private static class MockEventListener implements EventListener {

    }

    private static class MockServerConnection extends ServerConnection {
        private final ByteBufferPool bufferPool;
        private SSLSessionInfo sslSessionInfo;
        private XnioBufferPoolAdaptor poolAdaptor;

        private MockServerConnection(ByteBufferPool bufferPool) {
            this.bufferPool = bufferPool;
        }

        @Override
        public Pool<ByteBuffer> getBufferPool() {
            if (poolAdaptor == null) {
                poolAdaptor = new XnioBufferPoolAdaptor(getByteBufferPool());
            }
            return poolAdaptor;
        }


        @Override
        public ByteBufferPool getByteBufferPool() {
            return bufferPool;
        }

        @Override
        public XnioWorker getWorker() {
            return null;
        }

        @Override
        public XnioIoThread getIoThread() {
            return null;
        }

        @Override
        public HttpServerExchange sendOutOfBandResponse(HttpServerExchange exchange) {
            throw UndertowMessages.MESSAGES.outOfBandResponseNotSupported();
        }

        @Override
        public boolean isContinueResponseSupported() {
            return false;
        }

        @Override
        public void terminateRequestChannel(HttpServerExchange exchange) {

        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean supportsOption(Option<?> option) {
            return false;
        }

        @Override
        public <T> T getOption(Option<T> option) throws IOException {
            return null;
        }

        @Override
        public <T> T setOption(Option<T> option, T value) throws IllegalArgumentException, IOException {
            return null;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public SocketAddress getPeerAddress() {
            return null;
        }

        @Override
        public <A extends SocketAddress> A getPeerAddress(Class<A> type) {
            return null;
        }

        @Override
        public ChannelListener.Setter<? extends ConnectedChannel> getCloseSetter() {
            return null;
        }

        @Override
        public SocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public <A extends SocketAddress> A getLocalAddress(Class<A> type) {
            return null;
        }

        @Override
        public OptionMap getUndertowOptions() {
            return OptionMap.EMPTY;
        }

        @Override
        public int getBufferSize() {
            return 1024;
        }

        @Override
        public SSLSessionInfo getSslSessionInfo() {
            return sslSessionInfo;
        }

        @Override
        public void setSslSessionInfo(SSLSessionInfo sessionInfo) {
            sslSessionInfo = sessionInfo;
        }

        @Override
        public void addCloseListener(CloseListener listener) {
        }

        @Override
        public StreamConnection upgradeChannel() {
            return null;
        }

        @Override
        public ConduitStreamSinkChannel getSinkChannel() {
            return null;
        }

        @Override
        public ConduitStreamSourceChannel getSourceChannel() {
            return new ConduitStreamSourceChannel(null, null);
        }

        @Override
        protected StreamSinkConduit getSinkConduit(HttpServerExchange exchange, StreamSinkConduit conduit) {
            return conduit;
        }

        @Override
        protected boolean isUpgradeSupported() {
            return false;
        }

        @Override
        protected boolean isConnectSupported() {
            return false;
        }

        @Override
        protected void exchangeComplete(HttpServerExchange exchange) {
        }

        @Override
        protected void setUpgradeListener(HttpUpgradeListener upgradeListener) {
            //ignore
        }

        @Override
        protected void setConnectListener(HttpUpgradeListener connectListener) {
            //ignore
        }

        @Override
        protected void maxEntitySizeUpdated(HttpServerExchange exchange) {
        }

        @Override
        public String getTransportProtocol() {
            return "mock";
        }

        @Override
        public boolean isRequestTrailerFieldsSupported() {
            return false;
        }
    }

    protected static String getExceptionAllinformation(Exception ex) {
        String sOut = "";
        sOut += ex.getMessage() + "\r\n";
        StackTraceElement[] trace = ex.getStackTrace();
        for (StackTraceElement s : trace) {
            sOut += "\tat " + s + "\r\n";
        }
        return sOut;
    }
}
