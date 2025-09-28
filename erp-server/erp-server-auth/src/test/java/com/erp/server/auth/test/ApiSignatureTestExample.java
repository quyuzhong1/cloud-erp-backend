package com.erp.server.auth.test;

import com.alibaba.fastjson.JSON;
import com.common.core.enums.SignTypeEnum;
import com.common.core.utils.RsaEncryptUtil;
import com.erp.model.sys.dto.SsoLoginRequestDTO;
import com.erp.model.sys.enums.AppTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;
import java.util.Map;

/**
 * API签名测试示例
 * 演示如何使用 ApiSignUtil.generateSignatureHeader 生成 API-Signature
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
public class ApiSignatureTestExample {

//    appId: 8962cfaa0ba74c5db34988a6cbeae0de
//    appSecret: Dmv7wTe3Y-AmA0jwIFsWOhupVixvCxIT8M6O_gFTpSM
    public static void main(String[] args) {
//        // 示例1：生成单点登录API签名
//        generateSsoApiSignature();
//
//        // 示例2：生成不同HTTP方法的API签名
//        generateDifferentHttpMethodSignatures();
//
//        // 示例3：生成带查询参数的API签名
//        generateQueryParamSignatures();
//
//        // 示例4：验证API签名
        verifyApiSignatures();
//        generatePostmanExample();
    }

    /**
     * 示例1：生成单点登录API签名
     */
    public static void generateSsoApiSignature() {
        log.info("=== 示例1：生成单点登录API签名 ===");
        
        // 1. 生成测试数据
        String appId = "8962cfaa0ba74c5db34988a6cbeae0de";
        String unionId = SsoLoginTestHelper.generateTestUnionId();
        
        // 2. 使用数据库中的公钥（请替换为您的实际公钥）
        String publicKey = RsaEncryptUtil.extractPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5nMJXoFdk0GoK7fnQa9f\n" +
                "ye2NTdrSHfce/GBV10E30FqgiIVFY+R/2HnlBoaZmdF72la2whT3VYtrx9jDkOZX\n" +
                "gakXRf7IAWl1sfJLARMQkpUlwNPwbKN1Ahay95pFeoyxfev5pF69O0VkxeDmw+qm\n" +
                "xSFyC448QWx4ISZMwvzj+j32wbemt+yoBEAojcy4cxO8oIg2D4Pkh2a30JzpgyCr\n" +
                "+MiDgkfM/vL2d81ndxtQW7YZfO3893NGzxT8+TbgwsDVIFIaU5sUZ2rLBch/XUHu\n" +
                "/0RE2KiaF4Kcj0ojlfvKG//W/bX0OGOy5QnF9FXQhAFF71jWNkAkf/CEjD6u1YS+\n" +
                "gQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n"); // 请替换为数据库中实际的Base64编码公钥
        
        // 3. 生成单点登录请求
        SsoLoginRequestDTO request = SsoLoginTestHelper.generateFeishuSsoRequest(unionId, publicKey);
        String requestBody = JSON.toJSONString(request);
        
        // 4. 生成API签名
        String secretKey = "your_app_secret_key";
        String signatureHeader = SsoLoginTestHelper.generateSsoApiSignature(requestBody, secretKey, SignTypeEnum.HMAC.getCode());
        
        // 5. 生成完整的请求头
        Map<String, String> headers = SsoLoginTestHelper.generateSsoRequestHeaders(requestBody, appId, secretKey, SignTypeEnum.HMAC.getCode());
        
        log.info("单点登录请求体: {}", requestBody);
        log.info("API-Signature: {}", signatureHeader);
        log.info("完整请求头:");
        headers.forEach((key, value) -> log.info("  {}: {}", key, value));
        log.info("");
    }

    /**
     * 示例2：生成不同HTTP方法的API签名
     */
    public static void generateDifferentHttpMethodSignatures() {
        log.info("=== 示例2：生成不同HTTP方法的API签名 ===");
        
        String secretKey = "your_secret_key";
        String requestBody = "{\"name\":\"张三\",\"email\":\"zhangsan@example.com\"}";
        
        // GET请求
        String getSignature = SsoLoginTestHelper.generateApiSignature("GET", "/api/users", "", null, secretKey, SignTypeEnum.HMAC.getCode());
        log.info("GET /api/users");
        log.info("API-Signature: {}", getSignature);
        
        // POST请求
        String postSignature = SsoLoginTestHelper.generateApiSignature("POST", "/api/users", requestBody, null, secretKey, SignTypeEnum.HMAC.getCode());
        log.info("POST /api/users");
        log.info("API-Signature: {}", postSignature);
        
        // PUT请求
        String putSignature = SsoLoginTestHelper.generateApiSignature("PUT", "/api/users/123", requestBody, null, secretKey, SignTypeEnum.HMAC.getCode());
        log.info("PUT /api/users/123");
        log.info("API-Signature: {}", putSignature);
        
        // DELETE请求
        String deleteSignature = SsoLoginTestHelper.generateApiSignature("DELETE", "/api/users/123", "", null, secretKey, SignTypeEnum.HMAC.getCode());
        log.info("DELETE /api/users/123");
        log.info("API-Signature: {}", deleteSignature);
        log.info("");
    }

    /**
     * 示例3：生成带查询参数的API签名
     */
    public static void generateQueryParamSignatures() {
        log.info("=== 示例3：生成带查询参数的API签名 ===");
        
        String secretKey = "your_secret_key";
        
        // 带单个查询参数
        String singleParamSignature = SsoLoginTestHelper.generateApiSignatureWithQuery(
            "GET", "/api/users", "", secretKey, SignTypeEnum.HMAC.getCode(), 
            "page", "1");
        log.info("GET /api/users?page=1");
        log.info("API-Signature: {}", singleParamSignature);
        
        // 带多个查询参数
        String multiParamSignature = SsoLoginTestHelper.generateApiSignatureWithQuery(
            "GET", "/api/users", "", secretKey, SignTypeEnum.HMAC.getCode(), 
            "page", "1", "size", "10", "sort", "name");
        log.info("GET /api/users?page=1&size=10&sort=name");
        log.info("API-Signature: {}", multiParamSignature);
        
        // 带查询参数和请求体
        String bodyWithQuerySignature = SsoLoginTestHelper.generateApiSignatureWithQuery(
            "POST", "/api/users/search", "{\"keyword\":\"张三\"}", secretKey, SignTypeEnum.HMAC.getCode(), 
            "page", "1", "size", "10");
        log.info("POST /api/users/search?page=1&size=10");
        log.info("API-Signature: {}", bodyWithQuerySignature);
        log.info("");
    }

    /**
     * 示例4：验证API签名
     */
    public static void verifyApiSignatures() {
        log.info("=== 示例4：验证API签名 ===");
        
        String secretKey = "d2KmS0aD2vwcVy1Wowc6OVPIsbIbEzmu";
        String requestBody ="{\"method\":\"wmsWarehouseList\",\"data\":\"\"}";
        String httpMethod = "POST";
        String uri = "/open/api/service/v2";
        // 生成签名
        String signatureHeader = SsoLoginTestHelper.generateApiSignature(httpMethod, uri, requestBody, null, secretKey, SignTypeEnum.HMAC.getCode());
        log.info("原始签名头: {}", signatureHeader);
        
        // 解析签名
        String[] parsed = SsoLoginTestHelper.parseApiSignature(signatureHeader);
        if (parsed != null && parsed.length == 2) {
            String timestamp = parsed[0];
            String signature = parsed[1];
            
            log.info("解析的时间戳: {}", timestamp);
            log.info("解析的签名: {}", signature);
            
            // 验证正确的签名
            boolean isValid = SsoLoginTestHelper.verifyApiSignature(httpMethod, uri, requestBody, null, secretKey, SignTypeEnum.HMAC.getCode(), 
                                                                   signature, Long.parseLong(timestamp));
            log.info("正确密钥验证结果: {}", isValid ? "成功" : "失败");
            
            // 验证错误的密钥
            boolean isInvalid = SsoLoginTestHelper.verifyApiSignature(httpMethod, uri, requestBody, null, "wrong_key", SignTypeEnum.HMAC.getCode(), 
                                                                     signature, Long.parseLong(timestamp));
            log.info("错误密钥验证结果: {}", isInvalid ? "成功" : "失败");
            
            // 验证错误的请求体
            boolean isBodyInvalid = SsoLoginTestHelper.verifyApiSignature(httpMethod, uri, "{\"wrong\":\"data\"}", null, secretKey, SignTypeEnum.HMAC.getCode(),
                                                                         signature, Long.parseLong(timestamp));
            log.info("错误请求体验证结果: {}", isBodyInvalid ? "成功" : "失败");
        }
        log.info("");
    }

    /**
     * 生成用于Postman测试的完整示例
     */
    public static void generatePostmanExample() {
        log.info("=== Postman测试示例 ===");
        
        // 单点登录测试
        String appId = "8962cfaa0ba74c5db34988a6cbeae0de";
        String secretKey = "Dmv7wTe3Y-AmA0jwIFsWOhupVixvCxIT8M6O_gFTpSM";
        
        // 使用数据库中的公钥（请替换为您的实际公钥）
        String publicKey = RsaEncryptUtil.extractPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5nMJXoFdk0GoK7fnQa9f\n" +
                "ye2NTdrSHfce/GBV10E30FqgiIVFY+R/2HnlBoaZmdF72la2whT3VYtrx9jDkOZX\n" +
                "gakXRf7IAWl1sfJLARMQkpUlwNPwbKN1Ahay95pFeoyxfev5pF69O0VkxeDmw+qm\n" +
                "xSFyC448QWx4ISZMwvzj+j32wbemt+yoBEAojcy4cxO8oIg2D4Pkh2a30JzpgyCr\n" +
                "+MiDgkfM/vL2d81ndxtQW7YZfO3893NGzxT8+TbgwsDVIFIaU5sUZ2rLBch/XUHu\n" +
                "/0RE2KiaF4Kcj0ojlfvKG//W/bX0OGOy5QnF9FXQhAFF71jWNkAkf/CEjD6u1YS+\n" +
                "gQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
        SsoLoginRequestDTO request = SsoLoginTestHelper.generateFeishuSsoRequest("on_3bbb75fc9a3d428a8a7add17d81d66f8", publicKey);
        String requestBody = JSON.toJSONString(request);
        
        // 生成请求头
        Map<String, String> headers = SsoLoginTestHelper.generateSsoRequestHeaders(requestBody, appId, secretKey, SignTypeEnum.HMAC.getCode());
        
        log.info("Postman请求配置:");
        log.info("URL: POST http://localhost:8080/sso/login");
        log.info("Headers:");
        headers.forEach((key, value) -> log.info("  {}: {}", key, value));
        log.info("Body (raw JSON):");
        log.info("  {}", requestBody);
        log.info("");
        
        // 状态检查测试
        String statusSignature = SsoLoginTestHelper.generateApiSignatureWithQuery(
            "GET", "/sso/status", "", secretKey, SignTypeEnum.HMAC.getCode(), 
            "signSessionId", "test_session_123", "appId", appId);
        
        log.info("状态检查请求配置:");
        log.info("URL: GET http://localhost:8080/sso/status?signSessionId=test_session_123&appId={}", appId);
        log.info("Headers:");
        log.info("  API-Signature: {}", statusSignature);
        log.info("");
    }
}
