package com.kakarote.crm9;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.config.JFinalConfig;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.plugin.redis.RedisPlugin;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.jfinal.test.ControllerTestCase;
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
import org.xnio.*;
import org.xnio.channels.ConnectedChannel;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;
import org.xnio.conduits.StreamSinkConduit;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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
public class BaseControllerTest<T extends JFinalConfig> extends ControllerTestCase<T> {

    private static Log logger = Log.getLog(BaseControllerTest.class);

    @Inject
    private AdminUserService adminUserService = Aop.get(AdminUserService.class);

    @Inject
    private AdminRoleService adminRoleService = Aop.get(AdminRoleService.class);

    private static ActiveRecordPlugin activeRecord;
    private static RedisPlugin redisPlugin;

//    @Before
//    public void setUp() {
//        CrmProps props = CrmProps.getInstance();
//        DruidPlugin druidPlugin = createDruidPlugin(props);
//        activeRecord = new ActiveRecordPlugin(druidPlugin);
//        activeRecord.setDevMode(true);
//        activeRecord.setShowSql(true);
//        MappingKit.mapping(activeRecord);
//        JfinalConfig.getSqlTemplate(PathKit.getRootClassPath() + "/template", activeRecord);
//
//        createRedisPin(props);
//
//
//        druidPlugin.start();
//        activeRecord.start();
//        redisPlugin.start();
//
//    }

//    @After
//    public void tearDown() {
//        activeRecord.stop();
//        redisPlugin.stop();
//    }

    private void createRedisPin(CrmProps crmProp) {
        for (String configName : crmProp.get("jfinal.redis", "").split(",")) {
            if (crmProp.getBoolean(configName + ".open", Boolean.FALSE)) {
                if (crmProp.containsKey(configName + ".password") && StrUtil.isNotEmpty(crmProp.get(configName + ".password"))) {
                    redisPlugin = new RedisPlugin(crmProp.get(configName + ".cacheName").trim(), crmProp.get(configName + ".host").trim(), crmProp.getInt(configName + ".port", 6379), crmProp.getInt(configName + ".timeout", 20000), crmProp.get(configName + ".password", null), crmProp.getInt(configName + ".database", 0));
                } else {
                    redisPlugin = new RedisPlugin(crmProp.get(configName + ".cacheName").trim(), crmProp.get(configName + ".host").trim(), crmProp.getInt(configName + ".port", 6379), crmProp.getInt(configName + ".timeout", 20000));
                }
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

    public void mockBaseUtil(String staffNo) {
        CrmUser crmUser = mockCrmUser(staffNo);
        if (Objects.nonNull(crmUser)) {
            String bucToken = "CRM:UNITTEST:MOCK:BASEUTIL_" + staffNo;
            Redis.use().set(bucToken, crmUser);
            Redis.use().expire(bucToken, 300);
            Map<String, String> headers = new HashMap<>();
            headers.put(CrmConstant.BUC_AUTH_TOKEN_KEY, bucToken);
            mockRequest(headers, true);
        }
    }

    public HttpServletRequest mockRequestWithUser(String staffNo) {
        CrmUser crmUser = mockCrmUser(staffNo);
        if (Objects.nonNull(crmUser)) {
            String bucToken = "CRM:UNITTEST:MOCK:BASEUTIL_" + staffNo;
            Redis.use().set(BaseUtil.LOGIN_CACHE + bucToken, crmUser);
            Redis.use().expire(BaseUtil.LOGIN_CACHE + bucToken, 300);
            Map<String, String> headers = new HashMap<>();
            headers.put(CrmConstant.BUC_AUTH_TOKEN_KEY, bucToken);
            return mockRequest(headers, false);
        }
        return null;
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
}
