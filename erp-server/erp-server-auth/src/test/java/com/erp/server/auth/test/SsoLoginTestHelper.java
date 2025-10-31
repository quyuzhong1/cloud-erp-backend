package com.erp.server.auth.test;

import com.alibaba.fastjson.JSON;
import com.common.core.enums.SignTypeEnum;
import com.common.core.utils.ApiSignUtil;
import com.common.core.utils.RsaEncryptUtil;
import com.erp.model.sys.dto.SsoLoginRequestDTO;
import com.erp.model.sys.dto.SsoPayloadDTO;
import com.erp.model.sys.enums.AppTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 单点登录测试辅助类
 * 用于生成测试所需的请求参数
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
public class SsoLoginTestHelper {

    /**
     * 生成RSA密钥对（用于测试）
     */
    public static KeyPair generateTestKeyPair() {
        return RsaEncryptUtil.generateKeyPair(2048);
    }

    /**
     * 生成测试用的SsoLoginRequestDTO
     *
     * @param appType        应用类型
     * @param unionId        用户唯一标识
     * @param symmetricKey   对称密钥
     * @param publicKey      RSA公钥（Base64编码）
     * @return 加密后的请求DTO
     */
    public static SsoLoginRequestDTO generateSsoLoginRequest(
            String appType, 
            String unionId, 
            String symmetricKey, 
            String publicKey) {
        
        // 1. 构造payload
        SsoPayloadDTO payload = new SsoPayloadDTO();
        payload.setAppType(appType);
        payload.setUnionId(unionId);
        payload.setSymmetricKey(symmetricKey);
        
        // 2. 将payload转换为JSON字符串
        String payloadJson = JSON.toJSONString(payload);
        log.info("生成的payload JSON: {}", payloadJson);
        
        // 3. 使用RSA公钥加密payload
        String encryptedPayload = RsaEncryptUtil.encrypt(payloadJson, publicKey);
        log.info("加密后的payload: {}", encryptedPayload);
        
        // 4. 构造请求DTO
        SsoLoginRequestDTO request = new SsoLoginRequestDTO();
        request.setEncryptedPayload(encryptedPayload);
        
        return request;
    }

    /**
     * 生成飞书单点登录请求
     */
    public static SsoLoginRequestDTO generateFeishuSsoRequest(String unionId, String publicKey) {
        return generateSsoLoginRequest(
            AppTypeEnum.FS.getCode(),
            unionId,
            "test_symmetric_key_" + System.currentTimeMillis(),
            publicKey
        );
    }

    /**
     * 生成PDA单点登录请求
     */
    public static SsoLoginRequestDTO generatePdaSsoRequest(String unionId, String publicKey) {
        return generateSsoLoginRequest(
            AppTypeEnum.PDA.getCode(),
            unionId,
            "test_symmetric_key_" + System.currentTimeMillis(),
            publicKey
        );
    }

    /**
     * 生成微信小程序单点登录请求
     */
    public static SsoLoginRequestDTO generateWechatSsoRequest(String unionId, String publicKey) {
        return generateSsoLoginRequest(
            AppTypeEnum.WECHAT_MINIPROGRAM.getCode(),
            unionId,
            "test_symmetric_key_" + System.currentTimeMillis(),
            publicKey
        );
    }

    /**
     * 生成ERP系统单点登录请求
     */
    public static SsoLoginRequestDTO generateErpSsoRequest(String unionId, String publicKey) {
        return generateSsoLoginRequest(
            AppTypeEnum.ERP.getCode(),
            unionId,
            "test_symmetric_key_" + System.currentTimeMillis(),
            publicKey
        );
    }

    /**
     * 验证解密结果
     */
    public static boolean verifyDecryption(String encryptedPayload, String privateKey, 
                                         String expectedAppType, String expectedUnionId) {
        try {
            String decryptedPayload = RsaEncryptUtil.decrypt(encryptedPayload, privateKey);
            SsoPayloadDTO payload = JSON.parseObject(decryptedPayload, SsoPayloadDTO.class);
            
            return payload != null 
                && expectedAppType.equals(payload.getAppType())
                && expectedUnionId.equals(payload.getUnionId())
                && payload.getSymmetricKey() != null;
        } catch (Exception e) {
            log.error("验证解密失败", e);
            return false;
        }
    }

    /**
     * 生成测试用的App-Id
     */
    public static String generateTestAppId() {
        return "test_app_" + System.currentTimeMillis();
    }

    /**
     * 生成测试用的UnionId
     */
    public static String generateTestUnionId() {
        return "test_union_" + System.currentTimeMillis();
    }

    /**
     * 生成API签名头用于测试
     *
     * @param httpMethod   HTTP请求方法
     * @param uri          请求URI
     * @param requestBody  请求体
     * @param queryParams  查询参数
     * @param secretKey    密钥
     * @param signType     签名类型
     * @return API签名头
     */
    public static String generateApiSignature(String httpMethod, String uri, String requestBody, 
                                            Map<String, String> queryParams, String secretKey, String signType) {
        return ApiSignUtil.generateSignatureHeader(httpMethod, uri, requestBody, queryParams, secretKey, signType);
    }

    /**
     * 生成单点登录API签名头
     *
     * @param requestBody  单点登录请求体JSON
     * @param secretKey    应用密钥
     * @param signType     签名类型
     * @return API签名头
     */
    public static String generateSsoApiSignature(String requestBody, String secretKey, String signType) {
        return generateApiSignature("POST", "/sso/login", requestBody, null, secretKey, signType);
    }

    /**
     * 生成带查询参数的API签名头
     *
     * @param httpMethod   HTTP请求方法
     * @param uri          请求URI
     * @param requestBody  请求体
     * @param secretKey    密钥
     * @param signType     签名类型
     * @return API签名头
     */
    public static String generateApiSignatureWithQuery(String httpMethod, String uri, String requestBody, 
                                                      String secretKey, String signType, String... queryParams) {
        Map<String, String> queryMap = new HashMap<>();
        if (queryParams != null && queryParams.length > 0) {
            for (int i = 0; i < queryParams.length; i += 2) {
                if (i + 1 < queryParams.length) {
                    queryMap.put(queryParams[i], queryParams[i + 1]);
                }
            }
        }
        return generateApiSignature(httpMethod, uri, requestBody, queryMap, secretKey, signType);
    }

    /**
     * 验证API签名
     *
     * @param httpMethod    HTTP请求方法
     * @param uri           请求URI
     * @param requestBody   请求体
     * @param queryParams   查询参数
     * @param secretKey     密钥
     * @param signType      签名类型
     * @param signature     签名值
     * @param timestamp     时间戳
     * @return 验证结果
     */
    public static boolean verifyApiSignature(String httpMethod, String uri, String requestBody, 
                                           Map<String, String> queryParams, String secretKey, String signType, 
                                           String signature, long timestamp) {
        return ApiSignUtil.verifySignature(httpMethod, uri, requestBody, queryParams, secretKey, signType, 
                                         signature, timestamp);
    }

    /**
     * 解析API签名头
     *
     * @param signatureHeader 签名头字符串
     * @return 解析结果 [timestamp, signature]
     */
    public static String[] parseApiSignature(String signatureHeader) {
        return ApiSignUtil.parseSignatureHeader(signatureHeader);
    }

    /**
     * 生成完整的单点登录测试请求头
     *
     * @param requestBody  单点登录请求体JSON
     * @param appId        应用ID
     * @param secretKey    应用密钥
     * @param signType     签名类型
     * @return 请求头Map
     */
    public static Map<String, String> generateSsoRequestHeaders(String requestBody, String appId, 
                                                               String secretKey, String signType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("App-Id", appId);
        headers.put("Content-Type", "application/json");
        headers.put(ApiSignUtil.getSignatureHeaderName(), generateSsoApiSignature(requestBody, secretKey, signType));
        return headers;
    }

    /**
     * 打印测试信息
     */
    public static void printTestInfo(KeyPair keyPair, SsoLoginRequestDTO request, String appId) {
        log.info("=== 单点登录测试参数 ===");
        log.info("App-Id: {}", appId);
        log.info("公钥 (Base64): {}", RsaEncryptUtil.publicKeyToBase64(keyPair.getPublic()));
        log.info("私钥 (Base64): {}", RsaEncryptUtil.privateKeyToBase64(keyPair.getPrivate()));
        log.info("加密后的Payload: {}", request.getEncryptedPayload());
        log.info("========================");
    }

    /**
     * 打印API签名测试信息
     */
    public static void printApiSignatureTestInfo(String httpMethod, String uri, String requestBody, 
                                               String secretKey, String signType) {
        String signatureHeader = generateApiSignature(httpMethod, uri, requestBody, null, secretKey, signType);
        
        log.info("=== API签名测试参数 ===");
        log.info("HTTP方法: {}", httpMethod);
        log.info("请求URI: {}", uri);
        log.info("请求体: {}", requestBody);
        log.info("密钥: {}", secretKey);
        log.info("签名类型: {}", signType);
        log.info("API-Signature: {}", signatureHeader);
        
        // 解析并验证签名
        String[] parsed = parseApiSignature(signatureHeader);
        if (parsed != null && parsed.length == 2) {
            String timestamp = parsed[0];
            String signature = parsed[1];
            log.info("解析的时间戳: {}", timestamp);
            log.info("解析的签名: {}", signature);
            
            boolean isValid = verifyApiSignature(httpMethod, uri, requestBody, null, secretKey, signType, 
                                               signature, Long.parseLong(timestamp));
            log.info("签名验证结果: {}", isValid ? "成功" : "失败");
        }
        log.info("========================");
    }
}
