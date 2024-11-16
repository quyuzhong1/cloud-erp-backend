package com.common.core.security;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
public class HmacSHA256Utils {

    private HmacSHA256Utils() {
    }

    /**
     * HmacSHA256加密
     * @Author Luo_WG
     * @Date 2022/11/1 18:47
     * @param1 jsonString 请求参数的json字符串
     * @param2 secret 秘钥
     * @return java.lang.String
     **/
    public static String hmacSHA256(String jsonString, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(jsonString.getBytes(StandardCharsets.UTF_8));
            return byte2Hex(hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 十六进制编码
     * @Author Luo_WG
     * @Date 2022/11/14 10:25
     * @param bytes bytes
     * @return java.lang.String
     **/
    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
