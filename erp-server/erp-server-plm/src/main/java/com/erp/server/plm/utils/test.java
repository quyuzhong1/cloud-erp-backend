package com.erp.server.plm.utils;

import org.apache.commons.net.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class test {

    public static void main(String[] args) {
        //秘钥
        String secret= "13c324fa18feaaeb0ebcc8a7746ebfca";
        System.out.println("秘钥：" + secret);
        long l = System.currentTimeMillis()/1000;
        System.out.println(l);
        //请求参数json字符串
        String message="{\"data\":{\"expressTimeStart\":\"2022-11-13 22:50:00\",\"expressTimeEnd\":\"2022-11-14 22:50:59\"},\"appkey\":\"200780\",\"api\":\"order-get-order-list\",\"version\":1,\"timestamp\":\""+l+"\"}";
        System.out.println("请求参数json字符串：" + message);
        String authorization = hmacSHA256(message, secret);
        System.out.println("Authorization：" + authorization);
/*1667371801
Authorization：1a4d9fd73a4246bf2fce4b64c3c076fee15c00029b7d6fa74ad455f7cf27c13c*/

        System.out.println();
    }

    /**
     * hmac256加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] hmacSha256(String data, byte[] key) {
        String algorithm = "HmacSHA256";
        Mac sha256_HMAC;
        byte[] array = null;
        try {
            sha256_HMAC = Mac.getInstance(algorithm);
            sha256_HMAC.init(new SecretKeySpec(key, algorithm));
            array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return array;
    }
    /**
     * HmacSHA256加密
     * @Author Luo_WG
     * @Date 2022/11/1 18:47
     * @param1 jsonString 请求参数的json字符串
     * @param2 secret 秘钥
     * @return java.lang.String
     **/
    private static String hmacSHA256( String jsonString, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes("utf-8"), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hash = sha256_HMAC.doFinal(jsonString.getBytes("utf-8"));
            //String encodeStr = Base64.encodeBase64String(hash);
            return byte2Hex(hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
       return null;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
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
