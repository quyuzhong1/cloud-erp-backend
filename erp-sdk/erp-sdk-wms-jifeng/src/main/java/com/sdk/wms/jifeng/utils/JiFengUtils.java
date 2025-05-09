package com.sdk.wms.jifeng.utils;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JiFengUtils {

    public static final String SUCCESS = "Success";

    public static String sign(String key, String data) {
        return hmacsha256(key, data);
    }
    public static String sign(String key, Map<String, Object> params) {
        String data = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).sorted().collect(Collectors.joining("&"));
        return sign(key, data);
    }
    public static String hmacsha256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            hmac.init(secret_key);
            return new String(Hex.encodeHex(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
