package com.common.core.utils;

import com.common.core.enums.SignTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>
 * API请求签名工具类
 * 基于现有SignUtil的请求签名机制，支持AES和MD5签名算法
 * 按照API请求签名规范构建payload并生成签名
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
public class ApiSignUtil {

    private static final String SIGNATURE_HEADER = "API-Signature";
    private static final String TIMESTAMP_PARAM = "t";
    private static final String SIGNATURE_PARAM = "v";
    private static final String CHARSET = "UTF-8";
    private static final long REQUEST_TIMEOUT = 300; // 5分钟有效期

    /**
     * 生成API签名头
     *
     * @param httpMethod    HTTP请求方法（大写）
     * @param uri           请求的URI路径（不包含域名）
     * @param requestBody   请求体内容（JSON格式，无请求体时为空字符串）
     * @param queryParams   查询参数（无查询参数时为空）
     * @param secretKey     共享密钥
     * @param signType      签名类型（SignType.AES或SignType.MD5）
     * @return API签名头字符串
     */
    public static String generateSignatureHeader(String httpMethod, String uri, String requestBody, 
                                               Map<String, String> queryParams, String secretKey, String signType) {
        long timestamp = System.currentTimeMillis() / 1000;
        return generateSignatureHeader(httpMethod, uri, requestBody, queryParams, secretKey, signType, timestamp);
    }

    /**
     * 生成API签名头（指定时间戳）
     *
     * @param httpMethod    HTTP请求方法（大写）
     * @param uri           请求的URI路径（不包含域名）
     * @param requestBody   请求体内容（JSON格式，无请求体时为空字符串）
     * @param queryParams   查询参数（无查询参数时为空）
     * @param secretKey     共享密钥
     * @param signType      签名类型（SignType.AES或SignType.MD5）
     * @param timestamp     请求时间戳（Unix时间戳，秒）
     * @return API签名头字符串
     */
    public static String generateSignatureHeader(String httpMethod, String uri, String requestBody, 
                                               Map<String, String> queryParams, String secretKey, String signType, long timestamp) {
        // 构建签名载荷
        String payload = buildSignaturePayload(httpMethod, uri, requestBody, queryParams, timestamp);
        
        // 生成签名
        String signature = generateSignature(payload, secretKey, signType);
        
        // 构建签名头
        return String.format("t=%d,v=%s", timestamp, signature);
    }

    /**
     * 验证API签名
     *
     * @param httpMethod    HTTP请求方法（大写）
     * @param uri           请求的URI路径（不包含域名）
     * @param requestBody   请求体内容（JSON格式，无请求体时为空字符串）
     * @param queryParams   查询参数（无查询参数时为空）
     * @param secretKey     共享密钥
     * @param signType      签名类型（SignType.AES或SignType.MD5）
     * @param signature     签名值
     * @param timestamp     请求时间戳（Unix时间戳，秒）
     * @return 验证结果
     */
    public static boolean verifySignature(String httpMethod, String uri, String requestBody, 
                                        Map<String, String> queryParams, String secretKey, String signType, 
                                        String signature, long timestamp) {
        try {
            // 检查时间戳有效性
            if (!isTimestampValid(timestamp)) {
                log.warn("签名时间戳无效: {}", timestamp);
                return false;
            }

            // 构建签名载荷
            String payload = buildSignaturePayload(httpMethod, uri, requestBody, queryParams, timestamp);
            
            // 生成期望的签名
            String expectedSignature = generateSignature(payload, secretKey, signType);
            
            // 比较签名
            return StringUtils.equals(signature, expectedSignature);
        } catch (Exception e) {
            log.error("验证API签名失败", e);
            return false;
        }
    }

    /**
     * 从签名头中解析时间戳和签名
     *
     * @param signatureHeader 签名头字符串
     * @return 包含时间戳和签名的数组 [timestamp, signature]
     */
    public static String[] parseSignatureHeader(String signatureHeader) {
        if (StringUtils.isBlank(signatureHeader)) {
            return null;
        }

        try {
            String headerValue =signatureHeader.trim();
            String[] params = headerValue.split(",");
            
            String timestamp = null;
            String signature = null;
            
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    
                    if (TIMESTAMP_PARAM.equals(key)) {
                        timestamp = value;
                    } else if (SIGNATURE_PARAM.equals(key)) {
                        signature = value;
                    }
                }
            }
            
            if (StringUtils.isNotBlank(timestamp) && StringUtils.isNotBlank(signature)) {
                return new String[]{timestamp, signature};
            }
            
            return null;
        } catch (Exception e) {
            log.error("解析签名头失败: {}", signatureHeader, e);
            return null;
        }
    }

    /**
     * 构建签名载荷
     * 格式: HTTP_METHOD&URI&REQUEST_TIMESTAMP&REQUEST_PAYLOAD[&QUERY_STRING]
     *
     * @param httpMethod    HTTP请求方法
     * @param uri           请求的URI路径
     * @param requestBody   请求体内容
     * @param queryParams   查询参数
     * @param timestamp     请求时间戳
     * @return 签名载荷
     */
    private static String buildSignaturePayload(String httpMethod, String uri, String requestBody, 
                                              Map<String, String> queryParams, long timestamp) {
        StringBuilder payload = new StringBuilder();
        
        // HTTP_METHOD
        payload.append(StringUtils.upperCase(httpMethod));
        payload.append("&");
        
        // URI
        payload.append(uri);
        payload.append("&");
        
        // REQUEST_TIMESTAMP
        payload.append(timestamp);
        payload.append("&");
        
        // REQUEST_PAYLOAD
        payload.append(StringUtils.defaultString(requestBody));
        
        // QUERY_STRING（如果有查询参数）
        if (queryParams != null && !queryParams.isEmpty()) {
            payload.append("&");
            payload.append(buildQueryString(queryParams));
        }
        
        return payload.toString();
    }

    /**
     * 构建查询参数字符串
     *
     * @param queryParams 查询参数
     * @return 查询参数字符串
     */
    private static String buildQueryString(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }

        try {
            // 按key排序
            TreeMap<String, String> sortedParams = new TreeMap<>(queryParams);
            StringBuilder queryString = new StringBuilder();
            
            boolean first = true;
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (StringUtils.isBlank(entry.getValue())) {
                    continue;
                }
                
                if (!first) {
                    queryString.append("&");
                }
                first = false;
                
                queryString.append(URLEncoder.encode(entry.getKey(), CHARSET))
                          .append("=")
                          .append(URLEncoder.encode(entry.getValue(), CHARSET));
            }
            
            return queryString.toString();
        } catch (Exception e) {
            log.error("构建查询参数字符串失败", e);
            return "";
        }
    }

    /**
     * 生成签名
     * 使用现有的SignUtil.genSign方法进行签名，然后进行Base64编码
     *
     * @param payload   签名载荷
     * @param secretKey 共享密钥
     * @param signType  签名类型（SignType.AES或SignType.MD5）
     * @return 签名结果（Base64编码）
     */
    private static String generateSignature(String payload, String secretKey, String signType) {
        try {
            // 直接使用现有的SignUtil.genSign方法
            return  SignUtil.sign(payload,secretKey, SignTypeEnum.getByCode(signType));
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 检查时间戳是否有效
     *
     * @param timestamp 时间戳
     * @return 是否有效
     */
    private static boolean isTimestampValid(long timestamp) {
        long currentTime = System.currentTimeMillis() / 1000;
        long timeDiff = Math.abs(currentTime - timestamp);
        return timeDiff <= REQUEST_TIMEOUT;
    }

    /**
     * 验证签名头格式
     *
     * @param signatureHeader 签名头字符串
     * @return 是否有效
     */
    public static boolean isValidSignatureHeader(String signatureHeader) {
        String[] parsed = parseSignatureHeader(signatureHeader);
        return parsed != null && parsed.length == 2;
    }

    /**
     * 获取签名头名称
     *
     * @return 签名头名称
     */
    public static String getSignatureHeaderName() {
        return SIGNATURE_HEADER;
    }

    /**
     * 获取请求超时时间（秒）
     *
     * @return 超时时间
     */
    public static long getRequestTimeout() {
        return REQUEST_TIMEOUT;
    }

    /**
     * 使用示例：
     * 
     * // 1. 生成签名头
     * String httpMethod = "POST";
     * String uri = "/api/v1/users";
     * String requestBody = "{\"name\":\"张三\",\"email\":\"zhangsan@example.com\"}";
     * Map<String, String> queryParams = new HashMap<>();
     * queryParams.put("page", "1");
     * 
     * String secretKey = "your-secret-key";
     * String signType = SignType.AES; // 或 SignType.MD5
     * 
     * String signatureHeader = ApiSignatureUtil.generateSignatureHeader(
     *     httpMethod, uri, requestBody, queryParams, secretKey, signType);
     * 
     * // 2. 验证签名
     * String[] parsed = ApiSignatureUtil.parseSignatureHeader(signatureHeader);
     * if (parsed != null) {
     *     String timestamp = parsed[0];
     *     String signature = parsed[1];
     *     
     *     boolean isValid = ApiSignatureUtil.verifySignature(
     *         httpMethod, uri, requestBody, queryParams, secretKey, signType, 
     *         signature, Long.parseLong(timestamp));
     * }
     */
}
