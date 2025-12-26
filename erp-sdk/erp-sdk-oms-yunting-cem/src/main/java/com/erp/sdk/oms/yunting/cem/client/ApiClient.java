package com.erp.sdk.oms.yunting.cem.client;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 云听CEM API客户端
 *
 * @author ERP System
 */
@Slf4j
@Getter
@Setter
public class ApiClient {

    /**
     * 云听CEM开放平台基础URL
     */
    private String basePath = "https://opendata.yuntingai.com";

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * HTTP客户端
     */
    private OkHttpClient httpClient;

    /**
     * JSON序列化工具
     */
    private JSON json;

    /**
     * 默认请求头
     */
    private Map<String, String> defaultHeaderMap = new HashMap<>();

    /**
     * 连接超时时间（秒）
     */
    private int connectTimeout = 30;

    /**
     * 读取超时时间（秒）
     */
    private int readTimeout = 30;

    /**
     * 写入超时时间（秒）
     */
    private int writeTimeout = 30;

    /**
     * 构造函数
     */
    public ApiClient() {
        this.json = new JSON();
        this.httpClient = createDefaultHttpClient();
    }

    /**
     * 构造函数（带访问令牌）
     */
    public ApiClient(String accessToken) {
        this();
        this.accessToken = accessToken;
    }

    /**
     * 创建默认HTTP客户端
     */
    private OkHttpClient createDefaultHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 设置访问令牌
     */
    public ApiClient setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * 添加默认请求头
     */
    public void addDefaultHeader(String key, String value) {
        defaultHeaderMap.put(key, value);
    }

    /**
     * 执行GET请求
     */
    public <T> ApiResponse<T> executeGet(String path, Map<String, String> queryParams, Type returnType) throws ApiException {
        return execute("GET", path, queryParams, null, null, returnType);
    }

    /**
     * 执行POST请求
     */
    public <T> ApiResponse<T> executePost(String path, Object body, Map<String, String> headerParams, Type returnType) throws ApiException {
        return execute("POST", path, null, body, headerParams, returnType);
    }

    /**
     * 执行HTTP请求
     */
    public <T> ApiResponse<T> execute(String method, String path, Map<String, String> queryParams,
                                      Object body, Map<String, String> headerParams, Type returnType) throws ApiException {
        try {
            // 构建完整URL
            String url = buildUrl(path, queryParams);

            // 构建请求
            Request.Builder requestBuilder = new Request.Builder().url(url);

            // 添加默认请求头
            for (Map.Entry<String, String> entry : defaultHeaderMap.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            // 添加自定义请求头
            if (headerParams != null) {
                for (Map.Entry<String, String> entry : headerParams.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            // 添加认证头
            if (StringUtils.isNotBlank(accessToken)) {
                requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }

            // 添加Content-Type
            requestBuilder.addHeader("Content-Type", "application/json; charset=UTF-8");

            // 构建请求体
            RequestBody requestBody = null;
            if (body != null) {
                String jsonBody = json.serialize(body);
                requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),jsonBody);
            }

            // 设置请求方法和请求体
            if ("GET".equalsIgnoreCase(method)) {
                requestBuilder.get();
            } else if ("POST".equalsIgnoreCase(method)) {
                requestBuilder.post(requestBody != null ? requestBody : RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""));
            } else if ("PUT".equalsIgnoreCase(method)) {
                requestBuilder.put(requestBody != null ? requestBody : RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""));
            } else if ("DELETE".equalsIgnoreCase(method)) {
                requestBuilder.delete(requestBody);
            }

            Request request = requestBuilder.build();

            // 执行请求
            log.info("云听CEM API请求: {} {}", method, url);
            if (body != null) {
                log.debug("请求体: {}", json.serialize(body));
            }

            Response response = httpClient.newCall(request).execute();
            int statusCode = response.code();
            Map<String, List<String>> responseHeaders = response.headers().toMultimap();

            String responseBody = null;
            if (response.body() != null) {
                responseBody = response.body().string();
            }

            log.info("云听CEM API响应: 状态码={}, 响应体={}", statusCode, responseBody);

            // 检查HTTP状态码
            if (statusCode >= 200 && statusCode < 300) {
                // 成功响应
                T data = null;
                if (returnType != null && responseBody != null) {
                    data = json.deserialize(responseBody, returnType);
                }
                return new ApiResponse<>(statusCode, responseHeaders, data);
            } else {
                // 错误响应
                throw new ApiException(statusCode, "HTTP请求失败", responseHeaders, responseBody);
            }

        } catch (IOException e) {
            log.error("云听CEM API请求异常", e);
            throw new ApiException(e);
        }
    }

    /**
     * 构建完整URL
     */
    private String buildUrl(String path, Map<String, String> queryParams) {
        StringBuilder urlBuilder = new StringBuilder(basePath);
        urlBuilder.append(path);

        if (queryParams != null && !queryParams.isEmpty()) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                first = false;
                try {
                    urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    urlBuilder.append("=");
                    urlBuilder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    log.error("URL编码异常", e);
                }
            }
        }

        return urlBuilder.toString();
    }

    /**
     * 参数化类型解析
     */
    public static Type getParameterizedType(Class<?> rawType, Type... typeArguments) {
        return TypeToken.getParameterized(rawType, typeArguments).getType();
    }
}

