package com.sdk.wms.zhongbao.utils;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
/**
 * @author zdy
 * @ClassName AuthUtils
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */


public class AuthUtils {

    public static String getToken(String apiKey, String apiSecret) {
        //13位毫秒级时间戳
        Long timestamp = System.currentTimeMillis();
        //32位随机字符串
        String nonce = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        //生成签名
        String signature = generateSignature(apiSecret, timestamp, nonce);
        // 生成token: apiKey-timestamp-nonce-signature 的Base64编码
        return generateToken(apiKey, timestamp, nonce, signature);

    }
    // 生成token: apiKey-timestamp-nonce-signature 的Base64编码
    private static String generateToken(String apiKey, Long timestamp, String nonce, String signature) {
        String content = String.format("%s-%d-%s-%s", apiKey, timestamp, nonce, signature);
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    // 生成签名 (使用HMAC-SHA256)
    private static String generateSignature(String apiSecret, long timestamp, String nonce) {
        try {
            // 构建待签名字符串: 空字符串 + timestamp + nonce (用"-"连接)
            String dataToSign = timestamp + "-" + nonce;

            // 初始化HMAC-SHA256
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    apiSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(keySpec);

            // 计算签名并Base64编码
            byte[] signatureBytes = hmac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);

        } catch (Exception e) {
            throw new RuntimeException("API签名生成失败", e); // 替换为您的BusinessException
        }
    }
}