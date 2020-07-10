package com.kakarote.crm9.erp.crm.aliyun;

import com.jfinal.log.Log;
import com.kakarote.crm9.common.config.JfinalConfig;
import org.apache.commons.httpclient.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/1/6 11:32
 */
public class BatchTokenRetrieval {
    private static final Log logger = Log.getLog(BatchTokenRetrieval.class);
    /**
     * oauth2 获取授权码URL
     */
    private static final String ALIYUN_AUTHORITY_ENDPOINT = "https://signin.aliyun.com/oauth2/v1/auth";
    /**
     * 阿里云获取token链接
      */
    private static final String ALIYUN_TOKEN_ENDPOINT = "https://oauth.aliyun.com/v1/token";
    /**
     * 阿里云token撤销链接
     */
    private static final String ALIYUN_TOKEN_REVOKE_ENDPOINT = "https://oauth.aliyun.com/v1/revoke";
    /**
     * 阿里云呼叫中心ID
     */
    private static final String CLIENT_ID = JfinalConfig.crmProp.get("ccc.oauth.appid");
    /**
     * 阿里云呼叫中心secret
     */
    private static final String CLIENT_SECRET = JfinalConfig.crmProp.get("ccc.oauth.appsecretkey");

    /**
     * 获取阿里云完整认证url（包含参数）
     * @param nonce 随机字符串
     * @param state uuid
     * @return 完整url
     */
    public static String getAliyunAuthorityUrl(String nonce, String state, String redirectUri) {
        try {
            return ALIYUN_AUTHORITY_ENDPOINT
                    + "?response_type=code&response_mode=form_post"
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name())
                    + "&client_id=" + CLIENT_ID
                    + "&state=" + state
                    + "&codeChallenge=" + nonce
                    + "&access_type=offline";
        } catch (UnsupportedEncodingException e) {
            logger.error(String.format("method: getAliyunAuthorityUrl ,throw:%s", e.getMessage()));
        }
        return "";
    }

    /**
     * 2.用授权码换取访问令牌
     * @param authCode 授权码
     * @param nonce 随机字符串
     * @param redirectUri 回调地址由前端传达
     * @return 返回接口
     * @throws Exception 异常
     */
    public static String getAccessToken(String authCode, String nonce, String redirectUri)
            throws Exception {
        List<NameValuePair> params = new ArrayList<>(6);
        params.add(new NameValuePair("client_id", CLIENT_ID));
        params.add(new NameValuePair("grant_type", "authorization_code"));
        params.add(new NameValuePair("client_secret", CLIENT_SECRET));
        params.add(new NameValuePair("code", authCode));
        params.add(new NameValuePair("redirect_uri", redirectUri));
        params.add(new NameValuePair("code_verifier", nonce));
        NameValuePair[] pairs = new NameValuePair[params.size()];

        return HttpRequester.post(ALIYUN_TOKEN_ENDPOINT, params.toArray(pairs));
    }

    /**
     * 3。刷新访问令牌
     * @param refreshToken 刷新token
     * @return 返回接口
     * @throws Exception 异常
     */
    public static String refreshAccessToken(String refreshToken)
            throws Exception {
        List<NameValuePair> params = new ArrayList<>(4);
        params.add(new NameValuePair("client_id", BatchTokenRetrieval.CLIENT_ID));
        params.add(new NameValuePair("grant_type", "refresh_token"));
        params.add(new NameValuePair("client_secret", BatchTokenRetrieval.CLIENT_SECRET));
        params.add(new NameValuePair("refresh_token", refreshToken));
        NameValuePair[] pairs = new NameValuePair[params.size()];

        return HttpRequester.post(ALIYUN_TOKEN_ENDPOINT, params.toArray(pairs));
    }

    /**
     * 4.撤销访问令牌
     * @param accessToken 需要撤销的token
     * @param clientId 客户端id
     * @param clientSecret 客户端Secret
     * @return 返回接口
     * @throws Exception 异常
     */
    public static String revokeAccessToken(String accessToken, String clientId, String clientSecret)
            throws Exception {
        List<NameValuePair> params = new ArrayList<>(3);
        params.add(new NameValuePair("client_id", clientId));
        params.add(new NameValuePair("client_secret", clientSecret));
        params.add(new NameValuePair("token", accessToken));
        NameValuePair[] pairs = new NameValuePair[params.size()];

        return HttpRequester.post(ALIYUN_TOKEN_REVOKE_ENDPOINT, params.toArray(pairs));
    }

}
