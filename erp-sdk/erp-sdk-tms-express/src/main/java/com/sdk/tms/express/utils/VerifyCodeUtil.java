package com.sdk.tms.express.utils;

import com.common.core.exception.ServiceException;
import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
/**
 * @author zdy
 * @ClassName VerifyCodeUtil

 * @date 2023年10月30日
 * @version: 1.0
 */
public class VerifyCodeUtil {
    private VerifyCodeUtil(){}

    public static String md5EncryptAndBase64(String str) {
        return encodeBase64(md5Encrypt(str));
    }

    private static byte[] md5Encrypt(String encryptStr) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(encryptStr.getBytes("utf8"));
            return md5.digest();
        } catch (Exception e) {
            throw new ServiceException("MD5加密失败", e);
        }
    }

    private static String encodeBase64(byte[] b) {
        String str = (new Base64()).encodeAsString(b);
        return str;
    }
}
