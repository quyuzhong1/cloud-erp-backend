package com.common.core.utils;

import com.common.core.enums.SignTypeEnum;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * <p>
 * 对称密钥加签验签工具类
 * 支持AES和HMAC两种签名算法
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
public class SignUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String CHARSET = "UTF-8";

    /**
     * 根据算法类型进行签名
     *
     * @param data      待签名数据
     * @param key       密钥
     * @param algorithm 签名算法
     * @return 签名结果（Base64编码）
     */
    public static String sign(String data, String key, SignTypeEnum algorithm) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("待签名数据不能为空");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("签名算法不能为空");
        }

        switch (algorithm) {
            case AES:
                return aesSign(data, key);
            case HMAC:
                return hmacSign(data, key);
            default:
                throw new IllegalArgumentException("不支持的签名算法: " + algorithm);
        }
    }

    /**
     * 根据算法类型进行验签
     *
     * @param data      原始数据
     * @param signature 签名（Base64编码）
     * @param key       密钥
     * @param algorithm 签名算法
     * @return 验证结果
     */
    public static boolean verify(String data, String signature, String key, SignTypeEnum algorithm) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("原始数据不能为空");
        }
        if (signature == null || signature.trim().isEmpty()) {
            throw new IllegalArgumentException("签名不能为空");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("签名算法不能为空");
        }

        switch (algorithm) {
            case AES:
                return aesVerify(data, signature, key);
            case HMAC:
                return hmacVerify(data, signature, key);
            default:
                throw new IllegalArgumentException("不支持的签名算法: " + algorithm);
        }
    }

    /**
     * HMAC验证
     *
     * @param data      原始数据
     * @param signature 签名（Base64编码）
     * @param key       密钥
     * @return 验证结果
     */
    public static boolean hmacVerify(String data, String signature, String key) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("原始数据不能为空");
        }
        if (signature == null || signature.trim().isEmpty()) {
            throw new IllegalArgumentException("签名不能为空");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }

        try {
            String expectedSignature = hmacSign(data, key);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("HMAC验证失败", e);
            return false;
        }
    }

    /**
     * AES签名
     *
     * @param data 待签名数据
     * @param key  密钥
     * @return 签名结果（Base64编码）
     */
    public static String aesSign(String data, String key) {
        try {
            // 生成AES密钥
            SecretKeySpec secretKey = generateAesKey(key);
            
            // 使用AES密钥对数据进行HMAC签名
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            
            // 生成签名
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("AES签名失败", e);
            throw new RuntimeException("AES签名失败", e);
        }
    }

    /**
     * AES验签
     *
     * @param data      原始数据
     * @param signature 签名（Base64编码）
     * @param key       密钥
     * @return 验证结果
     */
    public static boolean aesVerify(String data, String signature, String key) {
        try {
            String expectedSignature = aesSign(data, key);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("AES验签失败", e);
            return false;
        }
    }

    /**
     * HMAC签名
     *
     * @param data 待签名数据
     * @param key  密钥
     * @return 签名结果（Base64编码）
     */
    public static String hmacSign(String data, String key) {
        try {
            // 创建HMAC对象
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKey);
            
            // 生成签名
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("HMAC签名失败", e);
            throw new RuntimeException("HMAC签名失败", e);
        }
    }

    /**
     * 生成AES密钥
     *
     * @param key 原始密钥
     * @return AES密钥
     */
    private static SecretKeySpec generateAesKey(String key) {
        try {
            // 使用SHA-256生成固定长度的密钥
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
            
            // 截取前32字节作为HMAC密钥
            byte[] hmacKey = new byte[32];
            System.arraycopy(keyBytes, 0, hmacKey, 0, 32);
            
            return new SecretKeySpec(hmacKey, "HmacSHA256");
        } catch (Exception e) {
            log.error("生成AES密钥失败", e);
            throw new RuntimeException("生成AES密钥失败", e);
        }
    }

    /**
     * 生成随机AES密钥
     *
     * @param keySize 密钥长度（字节）
     * @return Base64编码的密钥
     */
    public static String generateAesKey(int keySize) {
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] keyBytes = new byte[keySize];
            secureRandom.nextBytes(keyBytes);
            return Base64.getEncoder().encodeToString(keyBytes);
        } catch (Exception e) {
            log.error("生成随机AES密钥失败", e);
            throw new RuntimeException("生成随机AES密钥失败", e);
        }
    }

    /**
     * 生成随机HMAC密钥
     *
     * @param keySize 密钥长度（字节）
     * @return Base64编码的密钥
     */
    public static String generateHmacKey(int keySize) {
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] keyBytes = new byte[keySize];
            secureRandom.nextBytes(keyBytes);
            return Base64.getEncoder().encodeToString(keyBytes);
        } catch (Exception e) {
            log.error("生成随机HMAC密钥失败", e);
            throw new RuntimeException("生成随机HMAC密钥失败", e);
        }
    }

    /**
     * 根据算法类型生成密钥
     *
     * @param algorithm 签名算法
     * @return Base64编码的密钥
     */
    public static String generateKey(SignTypeEnum algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("签名算法不能为空");
        }

        switch (algorithm) {
            case AES:
                return generateAesKey(32); // 默认32字节
            case HMAC:
                return generateHmacKey(32); // 默认32字节
            default:
                throw new IllegalArgumentException("不支持的签名算法: " + algorithm);
        }
    }

    /**
     * 根据算法类型生成指定长度的密钥
     *
     * @param algorithm 签名算法
     * @param keySize   密钥长度
     * @return Base64编码的密钥
     */
    public static String generateKey(SignTypeEnum algorithm, int keySize) {
        if (algorithm == null) {
            throw new IllegalArgumentException("签名算法不能为空");
        }

        switch (algorithm) {
            case AES:
                return generateAesKey(keySize);
            case HMAC:
                return generateHmacKey(keySize);
            default:
                throw new IllegalArgumentException("不支持的签名算法: " + algorithm);
        }
    }
}
