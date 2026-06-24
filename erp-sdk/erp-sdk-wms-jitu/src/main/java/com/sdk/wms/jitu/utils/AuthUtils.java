package com.sdk.wms.jitu.utils;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zdy
 * @ClassName AuthUtils
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */

@Slf4j
public class AuthUtils {
    private static final String HEX_DIGITS[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    public static final String CHARSET_UTF8 = "UTF-8";
    /**
     * post请求 form-data格式：
     * @param url
     * @param param
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String,Object> param) throws IOException {
        String key = (String) param.getOrDefault("key","");
        String logistics_interface = (String) param.getOrDefault("logistics_interface","");
        String data_digest = generateSign(logistics_interface, key);
        Map<String,Object> map = new HashMap<>();
        if (CharSequenceUtil.isNotBlank(logistics_interface)){
            map.put("logistics_interface", param.get("logistics_interface"));
        }
        map.put("eccompanyid", param.get("eccompanyid"));
        map.put("data_digest", data_digest);
        if (CharSequenceUtil.isNotBlank((String) param.get("msg_type"))){
            map.put("msg_type", param.get("msg_type"));
        }
        Map<String, String> headers = new HashMap<>();
        log.warn("请求url：{}，请求参数：{}", url, JSONUtil.toJsonStr(map));
        return HttpUtil.createPost(url).form(map).contentType("multipart/form-data").addHeaders(headers)
                .timeout(10000).execute().body();
    }


    /**
     * 生成签名
     *
     * @param bizContent 入参  请求body加密 和上面logistics_interface取值一致
     * @param privateKey 秘钥
     * @return 签名
     */
    public static String generateSign(String bizContent, String privateKey) throws UnsupportedEncodingException {
        return new String(Base64.getEncoder().encode(code32(bizContent + privateKey, CHARSET_UTF8).getBytes(CHARSET_UTF8)));
    }

    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;

        if (n < 0) { // 修复：转义比较运算符
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return HEX_DIGITS[d1] + HEX_DIGITS[d2];
    }

    public static String code32(String origin, String charset) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charset == null || charset.isEmpty()) {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            } else {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charset)));
            }
        } catch (Exception exception) {

        }
        return resultString;
    }
}