package com.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @description: 钉钉消息发送工具类
 * @author: WanHongLei
 * @create: 2019-09-17 14:45
 **/
@Slf4j
public class DingDingMsgSendUtils{

    /**
     * 处理发送的钉钉消息
     *
     * @param accessToken a
     * @param textMsg t
     * @param secret secret
     */
    private static void dealDingDingMsgSend(String accessToken, String textMsg, String secret) {
        HttpClient httpclient = HttpClients.createDefault();
        StringBuilder sb = new StringBuilder("https://oapi.dingtalk.com/robot/send?access_token=").append(accessToken);

        //加签
        long timestamp = System.currentTimeMillis();
        try {
            String sign = generateSign(timestamp, secret);
            sb.append("&timestamp=").append(timestamp).append("&sign=").append(sign);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            log.error("【发送钉钉群消息】error：" + e.getMessage(), e);
        }

        HttpPost httppost = new HttpPost(sb.toString());
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");

        StringEntity se = new StringEntity(textMsg, "utf-8");
        httppost.setEntity(se);

        try {
            httpclient.execute(httppost);
        } catch (Exception e) {
            log.error("【发送钉钉群消息】error：" + e.getMessage(), e);
        }
    }

    /**
     * 发送钉钉群消息（可以艾特人）
     *
     * @param accessToken 群机器人accessToken
     * @param content     发送内容
     * @param secret     secret
     */
    public static void sendDingDingGroupMsg(String accessToken, String content, String secret) {
        content = content.replace("\"", "'");
        String textMsg;

        MsgDto msgDto = new MsgDto();
        msgDto.setMsgtype("text");

        TextDto textDto = new TextDto();
        textDto.setContent(content);
        msgDto.setText(textDto);

        AtDto atDto = new AtDto();
        atDto.setIsAtAll(false);

        /*List<String> result = Arrays.asList(atPhone.split(","));
        atDto.setAtMobiles(result);
        msgDto.setAt(atDto);*/

        textMsg = JSON.toJSONString(msgDto, SerializerFeature.WriteMapNullValue);
        dealDingDingMsgSend(accessToken, textMsg, secret);
    }

    /**
     *  获取签名
     * @param timestamp 时间戳
     * @param secret 签名字符串
     * @return sign
     * @throws NoSuchAlgorithmException n
     * @throws UnsupportedEncodingException u
     */
    private static String generateSign(long timestamp, String secret) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
    }

}

@Data
class AtDto {
    private List<String> atMobiles;
    private Boolean isAtAll = false;
}


@Data
class MsgDto {
    private String msgtype;
    private TextDto text;
    private AtDto at;
}

@Data
class TextDto {
    private String content;
}
