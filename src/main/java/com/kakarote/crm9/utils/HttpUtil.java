//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.kakarote.crm9.utils;

import com.jfinal.kit.StrKit;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUtil {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static String CHARSET = "UTF-8";
    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
    private static final HttpUtil.TrustAnyHostnameVerifier trustAnyHostnameVerifier = new HttpUtil.TrustAnyHostnameVerifier();

    private HttpUtil() {
    }

    private static SSLSocketFactory initSSLSocketFactory() {
        try {
            TrustManager[] tm = new TrustManager[]{new HttpUtil.TrustAnyTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init((KeyManager[])null, tm, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

    public static void setCharSet(String charSet) {
        if (StrKit.isBlank(charSet)) {
            throw new IllegalArgumentException("charSet can not be blank.");
        } else {
            CHARSET = charSet;
        }
    }

    private static HttpURLConnection getHttpConnection(String url, String method, Map<String, String> headers) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        URL url1 = new URL(url);
        HttpURLConnection conn = (HttpURLConnection)url1.openConnection();
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection)conn).setSSLSocketFactory(sslSocketFactory);
            ((HttpsURLConnection)conn).setHostnameVerifier(trustAnyHostnameVerifier);
        }
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (headers != null && !headers.isEmpty()) {
            Iterator var5 = headers.entrySet().iterator();

            while(var5.hasNext()) {
                Entry<String, String> entry = (Entry)var5.next();
                conn.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }

        return conn;
    }

    public static String get(String url, Map<String, String> queryParas, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = null;

        String var4;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), "GET", headers);
            conn.connect();
            var4 = readResponseString(conn);
        } catch (Exception var8) {
            throw new RuntimeException(var8);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

        }

        return var4;
    }

    public static String get(String url, Map<String, String> queryParas) throws Exception {
        return get(url, queryParas, (Map)null);
    }

    public static String get(String url) throws Exception {
        return get(url, (Map)null, (Map)null);
    }

    public static String post(String url, Map<String, String> queryParas, String data, Map<String, String> headers) {
        HttpURLConnection conn = null;

        String var11;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), "POST", headers);
            conn.connect();
            if (data != null) {
                OutputStream out = conn.getOutputStream();
                out.write(data.getBytes(CHARSET));
                out.flush();
                out.close();
            }

            var11 = readResponseString(conn);
        } catch (Exception var9) {
            throw new RuntimeException(var9);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

        }

        return var11;
    }

    public static String post(String url, Map<String, String> queryParas, String data) {
        return post(url, queryParas, data, (Map)null);
    }

    public static String post(String url, String data, Map<String, String> headers) {
        return post(url, (Map)null, data, headers);
    }

    public static String post(String url, String data) {
        return post(url, (Map)null, data, (Map)null);
    }

    private static String readResponseString(HttpURLConnection conn) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), CHARSET))) {
            String line = reader.readLine();
            String var4;
            if (line == null) {
                var4 = "";
                return var4;
            } else {
                StringBuilder ret = new StringBuilder();
                ret.append(line);

                while((line = reader.readLine()) != null) {
                    ret.append('\n').append(line);
                }

                var4 = ret.toString();
                return var4;
            }
        } catch (Exception var14) {
            throw new RuntimeException(var14);
        }
    }

    private static String buildUrlWithQueryString(String url, Map<String, String> queryParas) {
        if (queryParas != null && !queryParas.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            boolean isFirst;
            if (url.indexOf(63) == -1) {
                isFirst = true;
                sb.append('?');
            } else {
                isFirst = false;
            }

            String key;
            String value;
            for(Iterator var4 = queryParas.entrySet().iterator(); var4.hasNext(); sb.append(key).append('=').append(value)) {
                Entry<String, String> entry = (Entry)var4.next();
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append('&');
                }

                key = (String)entry.getKey();
                value = (String)entry.getValue();
                if (StrKit.notBlank(value)) {
                    try {
                        value = URLEncoder.encode(value, CHARSET);
                    } catch (UnsupportedEncodingException var9) {
                        throw new RuntimeException(var9);
                    }
                }
            }

            return sb.toString();
        } else {
            return url;
        }
    }

    public static String readData(HttpServletRequest request) {
        BufferedReader br = null;

        try {
            br = request.getReader();
            String line = br.readLine();
            if (line == null) {
                return "";
            } else {
                StringBuilder ret = new StringBuilder();
                ret.append(line);

                while((line = br.readLine()) != null) {
                    ret.append('\n').append(line);
                }

                return ret.toString();
            }
        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }

    /** @deprecated */
    @Deprecated
    public static String readIncommingRequestData(HttpServletRequest request) {
        return readData(request);
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
        private TrustAnyTrustManager() {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        	
        	// Do nothing
        }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        	
        	// Do nothing
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        private TrustAnyHostnameVerifier() {
        }
        @Override
        public boolean verify(String hostname, SSLSession session) {
            if(session != null){
                return true;
            }else{
                return false;
            }
        }
    }

    public static Map<String, String> buildJsonHeader(Map<String,String> headers) {
        Map<String, String> header = new HashMap<>(2);
        header.put("Content-Type", "application/json; charset=utf-8");
        if (headers != null && !headers.isEmpty()) {
            header.putAll(headers);
        }
        return header;
    }
}
