package com.kakarote.crm9.utils;

import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.common.annotation.ModelData;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.UserExpandInfo;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.mobile.common.MobileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * BaseUtil跟controller层的http servlet request强耦合，该类中的方法只在controller中使用，
 * 避免在service层使用，如果在service层使用，会对编写单测造成一些麻烦
 * @author honglei.wan
 */
public class BaseUtil {
    private static ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();

    public static String LOGIN_CACHE = "CRM:login_cache:";

    /**
     *
     * 获取当前系统是开发开始正式
     * @return true代表为真
     */
    public static boolean isDevelop() {
        return JfinalConfig.crmProp.getBoolean("jfinal.devMode",Boolean.TRUE);
    }

    /**
     * 获取当前是否是windows系统
     * @return true代表为真
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 签名数据
     *
     * @param key  key
     * @param salt 盐
     * @return 加密后的字符串
     */
    public static String sign(String key, String salt) {
        return DigestUtils.md5Hex((key + "erp" + salt).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证签名是否正确
     *
     * @param key  key
     * @param salt 盐
     * @param sign 签名
     * @return 是否正确 true为正确
     */
    public static boolean verify(String key, String salt, String sign) {
        return sign.equals(sign(key, salt));
    }

    /**
     * 获取当前年月的字符串
     *
     * @return yyyyMMdd
     */
    public static String getDate() {
        return DateUtil.format(new Date(), "yyyyMMdd");
    }

    public static String getIpAddress() {
        Prop prop = PropKit.use("config/undertow.txt");
        try {
            if (isDevelop()) {
                return "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + prop.get("undertow.port", "8080") + "/";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpServletRequest request=getRequest();
        /*
         * TODO nginx反向代理下手动增加一个请求头 proxy_set_header proxy_url "代理映射路径";
         * 如 location /api/ {
         *     proxy_set_header proxy_url "api"
         *     proxy_redirect off;
         * 	   proxy_set_header Host $host:$server_port;
         *     proxy_set_header X-Real-IP $remote_addr;
         * 	   proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         * 	   proxy_set_header X-Forwarded-Proto  $scheme;
         * 	   proxy_connect_timeout 60;
         * 	   proxy_send_timeout 120;
         * 	   proxy_read_timeout 120;
         *     proxy_pass http://127.0.0.1:8080/;
         *    }
         */
        String proxy=request.getHeader("proxy_url")!=null?"/"+request.getHeader("proxy_url"):"";
        return "http://" + request.getServerName()+":"+ request.getServerPort()+ request.getContextPath()+proxy+"/";
    }
    public static String getLoginAddress(HttpServletRequest request){
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.contains(",")) {
            return ip.split(",")[0];
        } else {
            return ip;
        }
    }

    public static String getLoginAddress() {
        return getLoginAddress(getRequest());
    }

    public static void setRequest(HttpServletRequest request) {
        threadLocal.set(request);
    }

    public static HttpServletRequest getRequest() {
        return threadLocal.get();
    }

    public static AdminUser getUser() {
        CrmUser crmUser = getCrmUser();
        if (Objects.nonNull(crmUser) && Objects.nonNull(crmUser.getCrmAdminUser())) {
            return crmUser.getCrmAdminUser();
        }
        return null;
    }

    public static CrmUser getCrmUser() {
        return Redis.use().get(getToken());
    }

    public static Long getUserId(){
        AdminUser adminUser = getUser();
        if (adminUser != null) {
            return adminUser.getUserId();
        } else {
            return null;
        }
    }

    public static Integer getDeptId() {
        AdminUser adminUser = getUser();
        return adminUser == null ? null : adminUser.getDeptId();
    }

    public static void removeThreadLocal(){
        threadLocal.remove();
    }

    public static String getToken(){
        return getToken(getRequest());
    }

    public static String getToken(HttpServletRequest request){
        String token = request.getHeader(CrmConstant.BUC_AUTH_TOKEN_KEY) != null ? request.getHeader(CrmConstant.BUC_AUTH_TOKEN_KEY) : getCookieValue(request,CrmConstant.BUC_AUTH_TOKEN_KEY);
        //兼容手机端token
        if (StringUtils.isBlank(token)){
            token = MobileUtil.getCrmToken(request);
        }
        return LOGIN_CACHE + token;
    }

    public static String getTokenWithOutPrefix(HttpServletRequest request){
        String token = request.getHeader(CrmConstant.BUC_AUTH_TOKEN_KEY) != null ? request.getHeader(CrmConstant.BUC_AUTH_TOKEN_KEY) : getCookieValue(request,CrmConstant.BUC_AUTH_TOKEN_KEY);
        //兼容手机端token
        if (StringUtils.isBlank(token)){
            token = MobileUtil.getCrmToken(request);
        }
        return token;
    }

    public static String getCookieValue(HttpServletRequest request,String name) {
        String cookieValue= "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookieValue = cookie.getValue();
                    break;
                }
            }
        }
        return cookieValue;
    }

    /**
     * Return exception stack trace.
     *
     * @param e
     * @return
     */
    public static String getExceptionStack(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        return baos.toString();
    }

    public static Record convertModel2Record(Model model) throws Exception {
        Record record = model.toRecord();
        for (Field field : model.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ModelData.class)) {
                field.setAccessible(true);
                record.set(field.getName(), field.get(model));
            }
        }
        return record;
    }

    public static List<Record> convertModelList2RecordList(List<? extends Model> models) throws Exception {
        if (CollectionUtils.isNotEmpty(models)) {
            List<Record> records = Lists.newArrayList();
            for (Model model : models) {
                records.add(convertModel2Record(model));
            }
            return records;
        }
        return Collections.emptyList();
    }

    public static Record convertPojo2Record(Object pojo) throws Exception {
        if (Objects.isNull(pojo)) {
            return null;
        }
        Map<String, Object> items = Maps.newHashMap();
        for (Field field : pojo.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            items.put(field.getName(), field.get(pojo));
        }
        return new Record().setColumns(items);
    }

    /**
     * Request for schedule task, ok page, or mobile, do not need sso login.
     *
     * @param request
     * @return
     */
    public static boolean needCheckLoginStatus(HttpServletRequest request) {
        String url = request.getRequestURI();
        return !(url.startsWith("/crm/integration") || url.endsWith("ok.html") || url.endsWith("amap.html") || url.endsWith(".png") || url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".map") || url.startsWith("/crm-mobile"));
    }

    /**
     * CRM部门列表
     */
    private static List<AdminDept> adminDeptList;

    public static List<AdminDept> getAdminDeptList() {
        return adminDeptList;
    }

    public static void setAdminDeptList(List<AdminDept> adminDeptList) {
        BaseUtil.adminDeptList = adminDeptList;
    }

    /**
     * 获取用户扩展信息
     * @return
     */
    public static UserExpandInfo getUserExpandInfo() {
        CrmUser crmUser = getCrmUser();
        if (Objects.nonNull(crmUser) && Objects.nonNull(crmUser.getCrmAdminUser())) {
            return getCrmUser().getUserExpandInfo();
        }
        return null;
    }
}
