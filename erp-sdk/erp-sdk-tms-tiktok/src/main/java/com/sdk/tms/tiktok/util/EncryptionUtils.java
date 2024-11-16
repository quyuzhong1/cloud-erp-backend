package com.sdk.tms.tiktok.util;

import cn.hutool.json.JSONUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EncryptionUtils {

    private EncryptionUtils(){}

    /**
     * 第一步提取除sign和access_token之外的所有查询参数。按字母顺序对参数的键重新排序
     * @param params 入参
     * @param path 接口
     * @param headerMap 请求头
     * @param secret 密钥
     * @param bodyStr 发起post请求的入参body
     * @return
     */
    public static String urlParamsSort(Map<String, Object> params, String path, Map<String, String> headerMap, String secret, String bodyStr) {
        // 提取除 "sign" 和 "access_token" 之外的所有查询参数
        List<String> keys = new ArrayList<>();
        for (String k : params.keySet()) {
            if (!"sign".equals(k) && !"access_token".equals(k)) {
                keys.add(k);
            }
        }

        // 按字母顺序对参数的键重新排序
        Collections.sort(keys);

        // 生成重新排序的查询键字符串
        StringBuilder input = new StringBuilder();
        for (String key : keys) {
            input.append(key).append(params.get(key));
        }

        input.insert(0, path);
        // 如果请求标头 content_type 不是 multipart/form-data，则追加到末尾 body
        if (!"multipart/form-data".equals(headerMap.get("content-type"))) {
            input.append(JSONUtil.toJsonStr(bodyStr));
        }
        String finalString = secret + input.toString() + secret;
        return finalString;
    }

    /**
     * 加密获取sign密钥
     * @param input
     * @param secret
     * @return
     */
    public static String generateSHA256(String input, String secret) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            hmacSha256.init(secretKey);
            byte[] hash = hmacSha256.doFinal(input.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace(); // 处理异常
            return null;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
