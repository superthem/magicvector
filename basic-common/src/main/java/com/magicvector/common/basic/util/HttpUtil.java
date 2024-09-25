package com.magicvector.common.basic.util;

import com.github.tbwork.anole.loader.Anole;
import com.github.tbwork.anole.loader.util.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static final RestTemplate restTemplate;

    static {
        int maxIdleConnections = Anole.getIntProperty("magic.vector.http.util.max-connections", 200);
        int keepAliveMinutes = Anole.getIntProperty("magic.vector.http.util.keepAlive-duration.minutes", 5);
        long readTimeout =  Anole.getIntProperty("magic.vector.http.util.read-timeout.seconds", 50);
        long connectTimeout = Anole.getIntProperty("magic.vector.http.util.connect-timeout.seconds", 15);
        // 创建 OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectionPool(new okhttp3.ConnectionPool(maxIdleConnections, keepAliveMinutes, TimeUnit.MINUTES)) // 配置连接池
                .readTimeout(readTimeout, TimeUnit.SECONDS) // 设置读取超时
                .connectTimeout(connectTimeout, TimeUnit.SECONDS) // 设置连接超时
                .build();

        // 配置 RestTemplate
        restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
    }

    /**
     * Sends an HTTP POST request with a JSON payload and returns a JSON response.
     */
    public static JsonObject postJson(String urlString, JsonObject jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; utf-8");
        headers.set("Accept", "application/json");

        HttpEntity<JsonObject> entity = new HttpEntity<>(jsonObject, headers);
        ResponseEntity<JsonObject> response = restTemplate.exchange(urlString, HttpMethod.POST, entity, JsonObject.class);

        return response.getBody();
    }

    public static JsonArray postStringAndGetArray(String urlString, String stringBody) {
        return JSON.parseArray(postStringAndGetString(urlString, stringBody));
    }

    public static String postStringAndGetString(String urlString, String stringBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; utf-8");
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(stringBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(urlString, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    public static JsonArray postJsonAndGetArray(String urlString, JsonObject jsonObject) {
        return JSON.parseArray(postJson(urlString, jsonObject).toString());
    }

    /**
     * Sends an HTTP GET request and returns a JSON response.
     */
    public static JsonObject getJson(String urlString) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonObject> response = restTemplate.exchange(urlString, HttpMethod.GET, entity, JsonObject.class);

        return response.getBody();
    }

    /**
     * Sends an HTTP GET request with cookies and returns a JSON response.
     */
    public static JsonObject getJson(String urlString, String cookieStr) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Cookie", cookieStr);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonObject> response = restTemplate.exchange(urlString, HttpMethod.GET, entity, JsonObject.class);

        return response.getBody();
    }
}