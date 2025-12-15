package cn.magicvector.common.basic.util;
import com.github.tbwork.anole.loader.Anole;
import com.github.tbwork.anole.loader.util.JSON;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpUtil {

    private static int maxIdleConnections = Anole.getIntProperty("okhttp.max-connections", 200);
    private static int keepAliveMinutes = Anole.getIntProperty("okhttp.keepAlive-duration.minutes", 5);
    private static long readTimeout =  Anole.getIntProperty("okhttp.read-timeout.seconds", 50);
    private static long connectTimeout = Anole.getIntProperty("okhttp.connect-timeout.seconds", 15);

    /**
     * da
     */
    private static final Map<String, OkHttpClient> proxiedClientMap = new ConcurrentHashMap<>();

    static {

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(Anole.getIntProperty("okhttp.requests.per.host.max", 10));
        dispatcher.setMaxRequests(Anole.getIntProperty("okhttp.requests.max", 100));
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.dispatcher(dispatcher)
                .connectionPool(new okhttp3.ConnectionPool(maxIdleConnections, keepAliveMinutes, TimeUnit.MINUTES))
                .readTimeout(readTimeout, TimeUnit.SECONDS) // 设置读取超时
                .connectTimeout(connectTimeout, TimeUnit.SECONDS) // 设置连接超时
                .build();
        proxiedClientMap.put("localhost", client); //localhost代表不使用任何代理

    }

    public static String get(String url) {
        return get(url, null, null);
    }

    public static String get(String url, Map<String, String> headers, Map<String, String> queries) {
        return coreGet(getOkHttpClient("localhost", 0, "", ""), url, headers, queries);
    }

    public static JsonObject post(String url, JsonObject body) {
        String result = post(url, body.toString());
        return JSON.parseObject(result, JsonObject.class);
    }

    public static String post(String url, String body) {
        return  post(url, body, null, null);
    }

    public static String post(String url, String body, Map<String, String> headers, Map<String, String> queries) {
        return corePost(getOkHttpClient("localhost", 0, "", ""), url, body, headers, queries);
    }


    public static String getViaProxy(String url, String proxyHost, int proxyPort){
        return getViaProxy(url, proxyHost, proxyPort, null, null);
    }

    public static String getViaProxy(String url, String proxyHost, int proxyPort,  Map<String, String> headers,  Map<String, String> queries){
        return getViaProxy(url, proxyHost, proxyPort, "", "", headers, queries);
    }

    public static String getViaProxy(String url, String proxyHost, int proxyPort, String username, String password, Map<String, String> headers, Map<String, String> queries){
        return coreGet(getOkHttpClient(proxyHost, proxyPort, username, password), url, headers, queries);
    }


    private static OkHttpClient getOkHttpClient(String proxyHost, int proxyPort, String username, String password){
        if(S.isEmpty(proxyHost) || "localhost".equals(proxyHost)){
            return proxiedClientMap.get("localhost");
        }
        String key = proxyHost+":"+proxyPort;
        if(!proxiedClientMap.containsKey(key)){
            synchronized (HttpUtil.class){
                if(!proxiedClientMap.containsKey(key)){
                    proxiedClientMap.put(key, doGetProxiedOkHttpClient(proxyHost, proxyPort, username, password));
                }
            }
        }
        return proxiedClientMap.get(key);
    }

    private static OkHttpClient doGetProxiedOkHttpClient(String proxyHost, int proxyPort, String username, String password){
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        Authenticator proxyAuthenticator = null;
        if(S.isNotEmpty(username)){
            proxyAuthenticator = new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    // 当响应返回 407（需要认证）时，设置认证头
                    if (response.code() == 407) {
                        // 在这里提供代理的用户名和密码
                        String credential = Credentials.basic(username, password);
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential) // 设置认证信息
                                .build();
                    }
                    return null; // 如果不是 407 状态码，则返回 null
                }
            };
        }
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(Anole.getIntProperty("okhttp.requests.per.host.max.proxy."+proxyHost+"."+proxyPort, 10));
        dispatcher.setMaxRequests(Anole.getIntProperty("okhttp.requests.max.proxy."+proxyHost+"."+proxyPort, 100));
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder = builder.dispatcher(dispatcher).proxy(proxy)
                .connectionPool(new okhttp3.ConnectionPool(maxIdleConnections, keepAliveMinutes, TimeUnit.MINUTES))
                .readTimeout(readTimeout, TimeUnit.SECONDS) // 设置读取超时
                .connectTimeout(connectTimeout, TimeUnit.SECONDS); // 设置连接超时

        if(proxyAuthenticator != null){
            builder = builder.proxyAuthenticator(proxyAuthenticator);
        }
        return builder.build();
    }


    public static JsonObject postViaProxy(String url, String host, int port, JsonObject body){
        OkHttpClient client = getOkHttpClient(host, port, "", "");
        String result = corePost(client, url, body.toString(), null, null);
        return JSON.parseObject(result, JsonObject.class);
    }

    public static String postViaProxy(String url, String host, int port, String body){
        OkHttpClient client = getOkHttpClient(host, port, "", "");
        return corePost(client, url, body, null, null);
    }

    public static String postViaProxy(String url, String host, int port, String body, Map<String, String> headers, Map<String, String> queries){
        OkHttpClient client = getOkHttpClient(host, port, "", "");
        return corePost(client, url, body, headers, queries);
    }


    public static String postViaProxy(String url, String host, int port, String username, String password, String body, Map<String, String> headers,  Map<String, String> queries){
        OkHttpClient client = getOkHttpClient(host, port, username, password);
        return corePost(client, url, body, headers, queries);
    }


    private static String coreGet(OkHttpClient client, String url, Map<String, String> headers, Map<String, String> queries){
        try {
            // 构造URL并附加查询参数
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            if (queries != null) {
                for (Map.Entry<String, String> entry : queries.entrySet()) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            String finalUrl = urlBuilder.build().toString();

            // 创建请求
            Request.Builder requestBuilder = new Request.Builder().url(finalUrl);

            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            // 执行请求
            Request request = requestBuilder.build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return response.body().string(); // 返回响应体
                } else {
                    throw new RuntimeException("远程服务器返回了失败状态！");
                }
            }
        } catch (Exception e) {
            log.error("从 {} 获取（GET）数据发生异常，具体信息：{}", url, e.getMessage(), e);
            return "ERROR: "+ e.getMessage();
        }
    }

    private static String corePost(OkHttpClient client, String url, String body, Map<String, String> headers, Map<String, String> queries){
        try {
            // 构造URL并附加查询参数
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            if (queries != null) {
                for (Map.Entry<String, String> entry : queries.entrySet()) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            String finalUrl = urlBuilder.build().toString();

            // 创建请求体
            RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json"));

            // 创建请求
            Request.Builder requestBuilder = new Request.Builder().url(finalUrl).post(requestBody);

            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            // 执行请求
            Request request = requestBuilder.build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return response.body().string(); // 返回响应体
                } else {
                    throw new RuntimeException("远程服务器返回了失败状态！");
                }
            }
        }  catch (Exception e) {
            log.error("Post到{}发生异常，具体信息：{}", url, e.getMessage(), e);
            return "ERROR: "+ e.getMessage();
        }
    }



}
