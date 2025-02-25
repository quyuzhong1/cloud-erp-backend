package com.erp.server.auth.utils;

import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Security;
import java.util.Arrays;

/**
 * AES 加解密
 */
@Slf4j
public class AESUtil {

    private static final Logger logger = LoggerFactory.getLogger(AESUtil.class);

    // 算法名称
    private static final String KEY_ALGORITHM = "AES";
//    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
//    private static final int GCM_TAG_LENGTH = 16; // GCM标签长度（16字节）
//    private static final int GCM_IV_LENGTH = 12; // GCM初始向量长度（12字节）



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




//------------以下代码为GCM模式加密解密，暂时不用--------------
//    public static void main(String[] args) {
//        try {
//            // 生成密钥
//            SecretKey secretKey = generateKey();
//            String keyStr = Base64.encodeBase64String(secretKey.getEncoded());
//            log.info("生成的密钥: {}", keyStr);
//
//            // 要加密的字符串
//            String originalString = "test";
//            log.info("原始字符串: {}", originalString);
//
//            // 加密
//            String encryptedString = encrypt(originalString, keyStr);
//            log.info("加密后的字符串: {}", encryptedString);
//
//            // 解密
//            String decryptedString = decrypt(encryptedString, keyStr);
//            log.info("解密后的字符串: {}", decryptedString);
//
//            // 验证加密和解密是否成功
//            if (originalString.equals(decryptedString)) {
//                log.info("加密和解密成功！");
//            } else {
//                log.error("加密和解密失败！");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * 生成密钥
//     *
//     * @return
//     * @throws Exception
//     */
//    public static SecretKey generateKey() {
//        try {
//            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
//            keyGenerator.init(128);
//            return keyGenerator.generateKey();
//        } catch (Exception e) {
//            logger.error("生成密钥失败", e);
//            throw new ServiceException("生成密钥失败");
//        }
//    }

//    public static String encrypt(String contentStr, String keyBytesStr, String charset) {
//
//        byte[] encryptedText = null;
//        try {
//            byte[] keyBytes = queryKeyByte(keyBytesStr, charset);
//            // 初始化
//            Security.addProvider(new BouncyCastleProvider());
//            // 转化成JAVA的密钥格式
//            Key key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
//            // 初始化cipher
//            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
//            byte[] iv = new byte[GCM_IV_LENGTH];
//            SecureRandom random = new SecureRandom();
//            random.nextBytes(iv);
//            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
//            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
//            byte[] encrypted = cipher.doFinal(contentStr.getBytes(charset));
//            byte[] encryptedWithIV = new byte[iv.length + encrypted.length];
//            System.arraycopy(iv, 0, encryptedWithIV, 0, iv.length);
//            System.arraycopy(encrypted, 0, encryptedWithIV, iv.length, encrypted.length);
//            encryptedText = encryptedWithIV;
//        } catch (Exception e) {
//            logger.error("加密失败", e);
//            throw new ServiceException("加密失败");
//        }
//        return org.apache.commons.codec.binary.Base64.encodeBase64String(encryptedText);
//    }

//    public static String decrypt(String encryptedDataStr, String keyBytesStr, String charset) {
//
//        byte[] encryptedText = null;
//        try {
//            byte[] keyBytes = queryKeyByte(keyBytesStr, charset);
//            // 初始化
//            Security.addProvider(new BouncyCastleProvider());
//            // 转化成JAVA的密钥格式
//            Key key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
//            // 初始化cipher
//            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
//            byte[] encryptedWithIV = org.apache.commons.codec.binary.Base64.decodeBase64(encryptedDataStr);
//            byte[] iv = Arrays.copyOfRange(encryptedWithIV, 0, GCM_IV_LENGTH);
//            byte[] encrypted = Arrays.copyOfRange(encryptedWithIV, GCM_IV_LENGTH, encryptedWithIV.length);
//            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
//            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
//            encryptedText = cipher.doFinal(encrypted);
//            return new String(encryptedText, charset);
//        } catch (Exception e) {
//            logger.error("解密失败", e);
//            throw new ServiceException("解密失败");
//        }
//    }
}
