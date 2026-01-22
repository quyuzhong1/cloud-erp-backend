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
        
        // 输出完整的请求报文
        logRequestInfo("POST", url, headers, null, requestBody);
        
        try {
            String responseBody = executePost(url, requestBody, headers);
            log.info("POST响应: {}", responseBody);
            
            return JSON.parseObject(responseBody, typeReference);
        } catch (Exception e) {
            // 输出完整的异常信息，包括原因链
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("POST请求失败\n");
            errorMsg.append("URL: ").append(url).append("\n");
            errorMsg.append("错误信息: ").append(e.getMessage()).append("\n");
            
            Throwable cause = e.getCause();
            int depth = 0;
            while (cause != null && depth < 10) {
                errorMsg.append("原因[").append(depth).append("]: ").append(cause.getClass().getName())
                        .append(" - ").append(cause.getMessage()).append("\n");
                if (cause.getStackTrace() != null && cause.getStackTrace().length > 0) {
                    StackTraceElement firstElement = cause.getStackTrace()[0];
                    errorMsg.append("  位置: ").append(firstElement.getClassName())
                            .append(".").append(firstElement.getMethodName())
                            .append("(").append(firstElement.getFileName())
                            .append(":").append(firstElement.getLineNumber()).append(")\n");
                }
                cause = cause.getCause();
                depth++;
            }
            
            log.error(errorMsg.toString(), e);
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
        
        // 解析查询参数
        String queryParams = extractQueryParams(url);
        
        // 输出完整的请求报文
        logRequestInfo("GET", url, headers, queryParams, null);
        
        try {
            String responseBody = executeGet(url, headers);
            log.info("GET响应: {}", responseBody);
            
            return JSON.parseObject(responseBody, typeReference);
        } catch (Exception e) {
            // 输出完整的异常信息，包括原因链
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("GET请求失败\n");
            errorMsg.append("URL: ").append(url).append("\n");
            errorMsg.append("错误信息: ").append(e.getMessage()).append("\n");
            
            Throwable cause = e.getCause();
            int depth = 0;
            while (cause != null && depth < 10) {
                errorMsg.append("原因[").append(depth).append("]: ").append(cause.getClass().getName())
                        .append(" - ").append(cause.getMessage()).append("\n");
                if (cause.getStackTrace() != null && cause.getStackTrace().length > 0) {
                    StackTraceElement firstElement = cause.getStackTrace()[0];
                    errorMsg.append("  位置: ").append(firstElement.getClassName())
                            .append(".").append(firstElement.getMethodName())
                            .append("(").append(firstElement.getFileName())
                            .append(":").append(firstElement.getLineNumber()).append(")\n");
                }
                cause = cause.getCause();
                depth++;
            }
            
            log.error(errorMsg.toString(), e);
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
        // 验证URL格式
        if (CharSequenceUtil.isBlank(url)) {
            throw new IllegalArgumentException("URL不能为空");
        }
        
        URL urlObj;
        try {
            urlObj = new URL(url);
        } catch (Exception e) {
            log.error("URL格式错误: {}", url, e);
            throw new IllegalArgumentException(CharSequenceUtil.format("URL格式错误: {}, 错误: {}", url, e.getMessage()), e);
        }
        
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

    /**
     * 输出完整的请求报文信息
     * 
     * @param method HTTP方法（GET/POST）
     * @param url 完整URL
     * @param headers Header Map
     * @param queryParams 查询参数字符串（GET请求）
     * @param body 请求体（POST请求）
     */
    private void logRequestInfo(String method, String url, Map<String, String> headers, String queryParams, String body) {
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("\n========== TF API 请求报文 ==========\n");
        logMsg.append("请求方法: ").append(method).append("\n");
        logMsg.append("请求URL: ").append(url).append("\n");
        
        // 输出Headers
        logMsg.append("请求Headers:\n");
        logMsg.append("  Content-Type: ").append(TfApiConstants.CONTENT_TYPE_JSON).append("\n");
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, value) -> {
                // 敏感信息脱敏处理（token、sign等）
                String displayValue = value;
                if (key != null && (key.toLowerCase().contains("token") )) {
                    if (value != null && value.length() > 10) {
                        displayValue = value.substring(0, 6) + "****" + value.substring(value.length() - 4);
                    }
                }
                logMsg.append("  ").append(key).append(": ").append(displayValue).append("\n");
            });
        }
        
        // 输出查询参数（GET请求）
        if (CharSequenceUtil.isNotBlank(queryParams)) {
            logMsg.append("查询参数: ").append(queryParams).append("\n");
        }
        
        // 输出请求体（POST请求）
        if (CharSequenceUtil.isNotBlank(body)) {
            logMsg.append("请求Body: ").append(body).append("\n");
        }
        
        logMsg.append("=====================================");
        log.info(logMsg.toString());
    }

    /**
     * 从URL中提取查询参数字符串
     * 
     * @param url 完整URL
     * @return 查询参数字符串（不包含?）
     */
    private String extractQueryParams(String url) {
        int queryIndex = url.indexOf('?');
        if (queryIndex >= 0 && queryIndex < url.length() - 1) {
            return url.substring(queryIndex + 1);
        }
        return null;
    }
}
