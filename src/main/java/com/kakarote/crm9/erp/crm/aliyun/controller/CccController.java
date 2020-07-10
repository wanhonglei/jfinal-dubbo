package com.kakarote.crm9.erp.crm.aliyun.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.BearerTokenCredentials;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.cache.RedisCache;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.aliyun.BatchTokenRetrieval;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/1/6 10:11
 */
@Before(IocInterceptor.class)
public class CccController extends Controller {

    private static final Log logger = Log.getLog(CccController.class);

    /**
     * 生产环境的Region是cn-shanghai
     */
    private static final String REGION = "cn-shanghai";
    private static final String PRODUCT = "CCC";
    private static final String ENDPOINT = "CCC";
    /**
     * 生产环境的domain是ccc.cn-shanghai.aliyuncs.com
     */
    private static final String DOMAIN = "ccc.cn-shanghai.aliyuncs.com";
    /**
     * 版本号
     */
    private static final String VERSION = "2017-07-05";

    /**
     * 用来生成nonce字符串，长度必须是45位
     */
    private final RandomStringUtils randomString = new RandomStringUtils(45);
    /**
     * session 过期时间
     */
    private static final int SESSION_ATTRIBUTE_STATES_TTL_SECONDS = 300;

    private static final String ACCESS_TOKEN = "CCC_AccessToken_";
    private static final String REFRESH_TOKEN = "CCC_RefreshToken_";
    private static final String CCC_NONCE = "CCC_NONCE_";

    /**
     * 通话录音查询使用的管理员账号
     */
    private static final String ADMIN_NAME = JfinalConfig.crmProp.get("ccc.admin.realname");

    @Inject
    private RedisCache redisCache;

    /**
     * 播放录音接口
     * @throws Exception 异常
     */
    public void playRecord() throws Exception {
        String action = BaseUtil.getRequest().getParameter("action");
        if ("ListRecordingsByContactId".equals(action) || "DownloadRecording".equals(action)
                || "DownloadAllTypeRecording".equals(action)){
            call();
        }else{
            JSONObject errorObj = new JSONObject();
            errorObj.put("HttpStatusCode",200);
            errorObj.put("Success",Boolean.FALSE);
            errorObj.put("RequestId","");
            errorObj.put("Code","ERROR");
            errorObj.put("Message","参数： action 非法");
            renderJson(errorObj);
        }
    }

    /**
     * 云呼叫中心请求接口
     * @throws Exception 异常
     */
    @Permissions("crm:ccc:call")
    public void call() throws Exception {
        HttpServletRequest httpServletRequest =  BaseUtil.getRequest();

        JSONObject errorObj = new JSONObject();
        errorObj.put("HttpStatusCode",200);
        errorObj.put("Success",Boolean.FALSE);
        errorObj.put("RequestId","");

        String action = httpServletRequest.getParameter("action");
        String request = httpServletRequest.getParameter("request");
        if (StringUtils.isBlank(action)){
            errorObj.put("Code","ERROR");
            errorObj.put("Message","参数： action 不能为空");
            renderJson(errorObj);
            return;
        }
        if (StringUtils.isBlank(request)){
            errorObj.put("Code","ERROR");
            errorObj.put("Message","参数： request 不能为空");
            renderJson(errorObj);
            return;
        }

        //默认工号为当前登陆人
        String num = BaseUtil.getUser().getNum();
        //当为下载录音接口时，工号切换成管理员的工号
        if ("ListRecordingsByContactId".equals(action) || "DownloadRecording".equals(action)
                || "DownloadAllTypeRecording".equals(action)){

            //admin_name对应管理员工号
            num = ADMIN_NAME;
            String refreshToken = redisCache.get(REFRESH_TOKEN + num);
            //当周倬的管理员刷新token不存在时，且不是周倬本人登陆，则返回错误提示
            if (StringUtils.isBlank(refreshToken) && !num.equals(BaseUtil.getUser().getNum())){
                errorObj.put("Code","NO_ADMIN");
                errorObj.put("Message","坐席管理员未授予录音播放权限");
                renderJson(errorObj);
                return;
            }
        }

        String accessToken = getDirectAccessToken(action,num);
        if (accessToken == null){
            errorObj.put("Code","ERROR");
            errorObj.put("Message","刷新token 获取访问token报错，请联系管理员");
            renderJson(errorObj);
            return;
        }

        String authCode = httpServletRequest.getParameter("authCode");
        String redirectUri = httpServletRequest.getParameter("redirectUri");
        String nonce = redisCache.get(CCC_NONCE + num);

        if (StringUtils.isNoneBlank(authCode,nonce,redirectUri)){
            try {
                accessToken = BatchTokenRetrieval.getAccessToken(authCode, nonce, redirectUri);
            } catch (Exception e) {
                logger.error(String.format("Failed to getAccessToken: %s",e));

                clearRedisToken(num);

                errorObj.put("Code","ERROR");
                errorObj.put("Message","获取AccessToken报错，请联系管理员");
                renderJson(errorObj);
                return;
            }
            logger.info(String.format("user num: %s ,getAccessToken return is %s", num, accessToken));

            JSONObject jsonObject = JSONObject.parseObject(accessToken);
            accessToken = jsonObject.getString("access_token");
            int expireTime = jsonObject.getIntValue("expires_in");
            redisCache.put(ACCESS_TOKEN + num,accessToken,expireTime, TimeUnit.SECONDS);

            String refreshToken = jsonObject.getString("refresh_token");
            redisCache.put(REFRESH_TOKEN + num,refreshToken,SESSION_ATTRIBUTE_STATES_TTL_SECONDS, TimeUnit.DAYS);

            redisCache.remove(CCC_NONCE + num);
        }

        if ("redirect".equals(accessToken)){
            if (StringUtils.isBlank(redirectUri)){
                errorObj.put("Code","ERROR");
                errorObj.put("Message","参数： redirectUri 不能为空");
                renderJson(errorObj);
                return;
            }

            nonce = redisCache.get(CCC_NONCE + num);
            if(StringUtils.isBlank(nonce)){
                nonce = randomString.nextString();
            }
            String state = UUID.randomUUID().toString();

            String authorityUrl = BatchTokenRetrieval.getAliyunAuthorityUrl(nonce, state, redirectUri);
            errorObj.put("Code","UNAUTHORIZED");
            errorObj.put("Message",authorityUrl);
            renderJson(errorObj);
            redisCache.put(CCC_NONCE + num, nonce, 5, TimeUnit.MINUTES);

            return;
        }

        invokeApiByBearerToken(accessToken, action, request);
    }

    /**
     * 直接获取accessToken
     * @return token 或者 异常标识
     */
    private String getDirectAccessToken(String action, String num){
        String accessToken = redisCache.get(ACCESS_TOKEN + num);
        String refreshToken = redisCache.get(REFRESH_TOKEN + num);

        //1.判断accessToken是否存在，存在则直接返回
        if (StringUtils.isNotBlank(accessToken)){
            return accessToken;
        }
        //2.判断refreshToken是否存在，存在则通过refreshToken获取accessToken
        if (StringUtils.isNotBlank(refreshToken)){
            try {
                String token = BatchTokenRetrieval.refreshAccessToken(refreshToken);
                JSONObject jsonObject = JSONObject.parseObject(token);
                accessToken = jsonObject.getString("access_token");
                int expireTime = jsonObject.getIntValue("expires_in");
                redisCache.put(ACCESS_TOKEN + num,accessToken,expireTime, TimeUnit.SECONDS);

                return accessToken;
            } catch (Exception e) {
                clearRedisToken(num);
                logger.error("获取accessToken出错", e);
                return null;
            }
        }else {
            //3.不存在refreshToken，则通过retrieve方法去获取refreshToken和accessToken信息
            return "redirect";
        }
    }

    /**
     * 通过token调用阿里云api
     * @param accessToken 访问token
     * @param action 动作参数
     * @param request 请求参数
     * @throws ClientException 异常
     */
    private void invokeApiByBearerToken(String accessToken, String action, String request) throws ClientException {
        DefaultProfile profileBear = DefaultProfile.getProfile(REGION);
        DefaultProfile.addEndpoint(ENDPOINT, REGION, PRODUCT, DOMAIN);

        BearerTokenCredentials bearerTokenCredentials =
                new BearerTokenCredentials(accessToken);
        DefaultAcsClient accessTokenClient = new DefaultAcsClient(profileBear, bearerTokenCredentials);
        accessTokenClient.setAutoRetry(false);

        /*
         * 使用CommonAPI调用POP API时，和使用传统产品SDK相比，请求和返回参数的格式都有所不同，因此需要进行一定的格式转换。
         */
        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setDomain(DOMAIN);
        commonRequest.setVersion(VERSION);
        commonRequest.setAction(action);
        JSONObject jsonObject = JSONObject.parseObject(request);

        for (Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey().trim();
            if (key.length() > 1) {
                key = key.substring(0, 1).toUpperCase() + key.substring(1);
            } else if (key.length() == 1) {
                key = key.toUpperCase();
            } else {
                continue;
            }
            commonRequest.putQueryParameter(key, entry.getValue() == null ? null : entry.getValue().toString());
        }

        commonRequest.putQueryParameter("accessToken", accessToken);
        CommonResponse response;
        try {
            response = accessTokenClient.getCommonResponse(commonRequest);
            logger.info("Aliyun returnData" + JSONObject.toJSONString(response.getData()));
        } catch (ClientException e) {
            clearRedisToken(null);

            logger.warn("Failed to invoke open API, request=" + JSON.toJSONString(commonRequest), e);
            JSONObject errorObj = new JSONObject();
            errorObj.put("HttpStatusCode",200);
            errorObj.put("Success",Boolean.FALSE);
            errorObj.put("RequestId","");

            errorObj.put("Code",e.getErrCode());
            errorObj.put("Message",e.getErrMsg());
            renderJson(errorObj);
            return;
        }

        renderJson(response.getData());
    }

    /**
     * 清除工号的redis缓存
     * @param num
     */
    private void clearRedisToken(String num){
        if (StringUtils.isBlank(num)){
            //登录状态才能打电话，这里不会为null
            num = BaseUtil.getUser().getNum();
        }

        redisCache.remove(REFRESH_TOKEN + num);
        redisCache.remove(ACCESS_TOKEN + num);
        redisCache.remove(CCC_NONCE + num);
    }

}
