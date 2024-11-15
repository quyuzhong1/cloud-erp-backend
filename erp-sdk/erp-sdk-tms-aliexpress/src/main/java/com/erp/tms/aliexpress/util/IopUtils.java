package com.erp.tms.aliexpress.util;


import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zdy
 * @ClassName IopUtils
 * @date 2023年11月14日
 * @version: 1.0
 */
public abstract class IopUtils {
    private IopUtils(){}

    private static String intranetIp;

    public static String signApiRequest(RequestContext requestContext, String appSecret, String signMethod) throws IOException {
        return signApiRequest(requestContext.getApiName(), requestContext.getAllParams(), null, appSecret, signMethod);
    }

    public static String signApiRequest(String apiName, Map<String, String> params, String body, String appSecret, String signMethod) throws IOException {
        String[] keys = (String[])params.keySet().toArray((Object[])new String[0]);
        Arrays.sort((Object[])keys);
        StringBuilder query = new StringBuilder();
        query.append(apiName);
        for (String key : keys) {
            String value = params.get(key);
            if (areNotEmpty(new String[] { key, value }))
                query.append(key).append(value);
        }
        if (body != null)
            query.append(body);
        byte[] bytes = null;
        if (signMethod.equals("sha256") || signMethod.equals("HmacSHA256")) {
            bytes = encryptHMACSHA256(query.toString(), appSecret);
        } else {
            throw new IOException("Invalid Sign Method");
        }
        return byte2hex(bytes);
    }

    private static byte[] encryptHMACSHA256(String data, String secret) throws IOException {
        byte[] bytes = null;
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes("UTF-8"));
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse.toString());
        }
        return bytes;
    }

    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1)
                sign.append("0");
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

    public static String getIntranetIp() {
        if (intranetIp == null)
            try {
                intranetIp = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                intranetIp = "127.0.0.1";
            }
        return intranetIp;
    }

    public static boolean isEmpty(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0)
            return true;
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(value.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean areNotEmpty(String... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (String value : values) {
            if (isEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    public static String formatDateTime(Date date, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
}
