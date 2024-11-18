package com.erp.server.auth.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Security;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.common.core.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * AES 加解密
 */
@Slf4j
public class AESUtil {

    private static final Logger logger = LoggerFactory.getLogger(AESUtil.class);

    // 算法名称
    private static final String KEY_ALGORITHM = "AES";

    /**
     * 加密
     *
     * @param contentStr  要加密的字符串
     * @param keyBytesStr 加密密钥
     * @return
     * @throws Exception
     */
    public static String encrypt(String contentStr, String keyBytesStr) {
        return encrypt(contentStr, keyBytesStr, StandardCharsets.UTF_8.displayName());
    }

    public static String encrypt(String contentStr, String keyBytesStr, String charset) {

        byte[] encryptedText = null;
        try {
            byte[] keyBytes = queryKeyByte(keyBytesStr, charset);
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            // 转化成JAVA的密钥格式
            Key key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            // 初始化cipher
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedText = cipher.doFinal(contentStr.getBytes(charset));
        } catch (Exception e) {
            logger.error("加密失败", e);
            throw new ServiceException("加密失败");
        }
        return Base64.encodeBase64String(encryptedText);
    }

    /**
     * 解密方法
     *
     * @param encryptedDataStr 要解密的字符串
     * @param keyBytesStr      解密密钥
     * @return
     */
    public static String decrypt(String encryptedDataStr, String keyBytesStr) {
        return decrypt(encryptedDataStr, keyBytesStr, StandardCharsets.UTF_8.displayName());
    }

    public static String decrypt(String encryptedDataStr, String keyBytesStr, String charset) {

        byte[] encryptedText = null;
        try {
            byte[] keyBytes = queryKeyByte(keyBytesStr, charset);
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            // 转化成JAVA的密钥格式
            Key key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            // 初始化cipher
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            encryptedText = cipher.doFinal(Base64.decodeBase64(encryptedDataStr));
            return new String(encryptedText, charset);
        } catch (Exception e) {
            logger.error("解密失败", e);
            throw new ServiceException("解密失败");
        }
    }

    public static void main(String[] args) {
        String key = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info(key);
        String sign = encrypt("test", key);
        log.info(sign);
        String str = decrypt(sign, key);
        log.info(str);
    }

    private static byte[] queryKeyByte(String keyBytesStr, String charset) {
        byte[] keyBytes = null;
		try {
			keyBytes = keyBytesStr.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException("加密不支持此字符");
		}
        // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
        int base = 16;
        if (keyBytes.length % base != 0) {
            int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
            byte[] temp = new byte[groups * base];
            Arrays.fill(temp, (byte) 0);
            System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
            keyBytes = temp;
        }
        return keyBytes;
    }

}
