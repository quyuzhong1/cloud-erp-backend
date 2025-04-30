package com.sdk.oms.temu.util;

import com.common.core.exception.ServiceException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EncryptionUtils {
    // 生成签名的方法
    public static String generateSignature(Map<String, Object> params, String secretKey) {
        // 对参数进行排序
        List<String> sortedKeys = new ArrayList<>(params.keySet());
        Collections.sort(sortedKeys);

        StringBuilder paramString = new StringBuilder();
        paramString.append(secretKey);
        for (String key : sortedKeys) {
            paramString.append(key).append(params.get(key));
        }
        // 添加密钥
        paramString.append(secretKey);

        try {
            // 使用MD5进行哈希计算
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(paramString.toString().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("签名错误",e);
        }
    }

}
