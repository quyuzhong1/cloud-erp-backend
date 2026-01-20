package com.sdk.third.tf.client;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.exception.ServiceException;
import com.sdk.third.tf.constant.TfApiConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * TF Fiscal API客户端
 * 使用模板方法模式统一处理HTTP请求
 * 
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
public class TfApiClient {

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;

    /**
     * 执行POST请求
     * 
     * @param path 请求路径
     * @param requestBody 请求体（JSON字符串）
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> T doPost(String path, String requestBody, Class<T> responseType) {
        return doPost(path, requestBody, new TypeReference<T>() {});
    }

    /**
     * 执行POST请求（支持泛型）
     * 
     * @param path 请求路径
     * @param requestBody 请求体（JSON字符串）
     * @param typeReference 响应类型引用
     * @return 响应对象
     */
    public <T> T doPost(String path, String requestBody, TypeReference<T> typeReference) {
        return doPost(path, requestBody, typeReference, null);
    }

    /**
     * 执行POST请求（支持自定义Header）
     * 
     * @param path 请求路径
     * @param requestBody 请求体（JSON字符串）
     * @param typeReference 响应类型引用
     * @param headers 自定义Header Map（key为Header名称，value为Header值）
     * @return 响应对象
     */
    public <T> T doPost(String path, String requestBody, TypeReference<T> typeReference, Map<String, String> headers) {
        String url = TfApiConstants.BASE_URL + path;
        log.debug("POST请求: {}, 请求体: {}", url, requestBody);
        
        try {
            String responseBody = executePost(url, requestBody, headers);
            log.debug("POST响应: {}", responseBody);
            
            return JSON.parseObject(responseBody, typeReference);
        } catch (Exception e) {
            log.error("POST请求失败: {}, 错误: {}", url, e.getMessage(), e);
            throw new ServiceException(CharSequenceUtil.format("API请求失败: {}", e.getMessage()));
        }
    }

    /**
     * 执行GET请求
     * 
     * @param path 请求路径（可包含查询参数）
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> T doGet(String path, Class<T> responseType) {
        return doGet(path, new TypeReference<T>() {});
    }

    /**
     * 执行GET请求（支持泛型）
     * 
     * @param path 请求路径（可包含查询参数）
     * @param typeReference 响应类型引用
     * @return 响应对象
     */
    public <T> T doGet(String path, TypeReference<T> typeReference) {
        return doGet(path, typeReference, null);
    }

    /**
     * 执行GET请求（支持自定义Header）
     * 
     * @param path 请求路径（可包含查询参数）
     * @param typeReference 响应类型引用
     * @param headers 自定义Header Map（key为Header名称，value为Header值）
     * @return 响应对象
     */
    public <T> T doGet(String path, TypeReference<T> typeReference, Map<String, String> headers) {
        String url = TfApiConstants.BASE_URL + path;
        log.debug("GET请求: {}", url);
        
        try {
            String responseBody = executeGet(url, headers);
            log.debug("GET响应: {}", responseBody);
            
            return JSON.parseObject(responseBody, typeReference);
        } catch (Exception e) {
            log.error("GET请求失败: {}, 错误: {}", url, e.getMessage(), e);
            throw new ServiceException(CharSequenceUtil.format("API请求失败: {}", e.getMessage()));
        }
    }

    /**
     * 执行POST请求（底层实现）
     */
    private String executePost(String url, String jsonStr) throws Exception {
        return executePost(url, jsonStr, null);
    }

    /**
     * 执行POST请求（底层实现，支持自定义Header）
     */
    private String executePost(String url, String jsonStr, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = createConnection(url, "POST", headers);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonStr.getBytes(StandardCharsets.UTF_8));
        }
        
        return handleResponse(conn);
    }

    /**
     * 执行GET请求（底层实现）
     */
    private String executeGet(String url) throws Exception {
        return executeGet(url, null);
    }

    /**
     * 执行GET请求（底层实现，支持自定义Header）
     */
    private String executeGet(String url, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = createConnection(url, "GET", headers);
        return handleResponse(conn);
    }

    /**
     * 创建HTTP连接
     */
    private HttpURLConnection createConnection(String url, String method) throws Exception {
        return createConnection(url, method, null);
    }

    /**
     * 创建HTTP连接（支持自定义Header）
     */
    private HttpURLConnection createConnection(String url, String method, Map<String, String> headers) throws Exception {
        URL urlObj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", TfApiConstants.CONTENT_TYPE_JSON);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        
        // 设置自定义Header
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(conn::setRequestProperty);
        }
        
        if ("POST".equals(method)) {
            conn.setDoOutput(true);
        }
        
        return conn;
    }

    /**
     * 处理HTTP响应
     */
    private String handleResponse(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        
        String responseBody;
        if (status >= 200 && status < 300) {
            responseBody = readStream(conn.getInputStream());
        } else {
            String error = readStream(conn.getErrorStream());
            log.error("HTTP请求失败, status: {}, error: {}", status, error);
            throw new ServiceException(CharSequenceUtil.format("HTTP请求失败, status: {}, error: {}", status, error));
        }
        
        return responseBody;
    }

    /**
     * 读取输入流为字符串
     */
    private String readStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * 构建查询参数字符串
     * 
     * @param params 参数Map
     * @return 查询参数字符串
     */
    public static String buildQueryString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder("?");
        params.forEach((key, value) -> {
            if (value != null) {
                if (sb.length() > 1) {
                    sb.append("&");
                }
                sb.append(key).append("=").append(value);
            }
        });
        
        return sb.length() > 1 ? sb.toString() : "";
    }
}
