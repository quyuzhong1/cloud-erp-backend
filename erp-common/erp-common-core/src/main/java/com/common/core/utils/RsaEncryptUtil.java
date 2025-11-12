package com.common.core.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <p>
 * RSA 公私钥加解密工具类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
public class RsaEncryptUtil {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String CHARSET = "UTF-8";

    /**
     * 使用公钥加密数据
     *
     * @param data      待加密数据
     * @param publicKey 公钥（Base64编码的X.509格式）
     * @return 加密结果（Base64编码）
     */
    public static String encrypt(String data, String publicKey) {
        try {
            // 将Base64编码的公钥转换为PublicKey对象
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            // 创建加密对象
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            // 加密数据
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA加密失败", e);
            throw new RuntimeException("RSA加密失败", e);
        }
    }

    /**
     * 使用私钥解密数据
     *
     * @param encryptedData 加密数据（Base64编码）
     * @param privateKey    私钥（Base64编码的PKCS#8格式）
     * @return 解密结果
     */
    public static String decrypt(String encryptedData, String privateKey) {
        try {
            // 将Base64编码的私钥转换为PrivateKey对象
            byte[] keyBytes = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);

            // 创建解密对象
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, priKey);

            // 解密数据
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw new RuntimeException("RSA解密失败", e);
        }
    }

    /**
     * 使用公钥加密数据（指定字符集）
     *
     * @param data      待加密数据
     * @param publicKey 公钥（Base64编码的X.509格式）
     * @param charset   字符集
     * @return 加密结果（Base64编码）
     */
    public static String encrypt(String data, String publicKey, String charset) {
        try {
            // 将Base64编码的公钥转换为PublicKey对象
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            // 创建加密对象
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            // 加密数据
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(charset));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA加密失败", e);
            throw new RuntimeException("RSA加密失败", e);
        }
    }

    /**
     * 使用私钥解密数据（指定字符集）
     *
     * @param encryptedData 加密数据（Base64编码）
     * @param privateKey    私钥（Base64编码的PKCS#8格式）
     * @param charset       字符集
     * @return 解密结果
     */
    public static String decrypt(String encryptedData, String privateKey, String charset) {
        try {
            // 将Base64编码的私钥转换为PrivateKey对象
            byte[] keyBytes = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);

            // 创建解密对象
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, priKey);

            // 解密数据
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, charset);
        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw new RuntimeException("RSA解密失败", e);
        }
    }

    /**
     * 分段加密（处理大数据）
     *
     * @param data      待加密数据
     * @param publicKey 公钥（Base64编码的X.509格式）
     * @return 加密结果（Base64编码）
     */
    public static String encryptByBlocks(String data, String publicKey) {
        try {
            // 将Base64编码的公钥转换为PublicKey对象
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            // 创建加密对象
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            // 获取密钥长度，计算分段大小
            int keyLength = ((java.security.interfaces.RSAPublicKey) pubKey).getModulus().bitLength() / 8;
            int maxBlockSize = keyLength - 11; // PKCS1Padding 需要11字节

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            int dataLength = dataBytes.length;
            int blockCount = (dataLength + maxBlockSize - 1) / maxBlockSize;

            StringBuilder encryptedData = new StringBuilder();
            for (int i = 0; i < blockCount; i++) {
                int start = i * maxBlockSize;
                int end = Math.min(start + maxBlockSize, dataLength);
                byte[] block = new byte[end - start];
                System.arraycopy(dataBytes, start, block, 0, end - start);

                byte[] encryptedBlock = cipher.doFinal(block);
                encryptedData.append(Base64.getEncoder().encodeToString(encryptedBlock));
            }

            return encryptedData.toString();
        } catch (Exception e) {
            log.error("RSA分段加密失败", e);
            throw new RuntimeException("RSA分段加密失败", e);
        }
    }

    /**
     * 分段解密（处理大数据）
     *
     * @param encryptedData 加密数据（Base64编码）
     * @param privateKey    私钥（Base64编码的PKCS#8格式）
     * @return 解密结果
     */
    public static String decryptByBlocks(String encryptedData, String privateKey) {
        try {
            // 将Base64编码的私钥转换为PrivateKey对象
            byte[] keyBytes = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);

            // 创建解密对象
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, priKey);

            // 获取密钥长度，计算分段大小
            int keyLength = ((java.security.interfaces.RSAPrivateKey) priKey).getModulus().bitLength() / 8;
            int blockSize = keyLength;

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            int encryptedLength = encryptedBytes.length;
            int blockCount = encryptedLength / blockSize;

            StringBuilder decryptedData = new StringBuilder();
            for (int i = 0; i < blockCount; i++) {
                int start = i * blockSize;
                int end = start + blockSize;
                byte[] block = new byte[blockSize];
                System.arraycopy(encryptedBytes, start, block, 0, blockSize);

                byte[] decryptedBlock = cipher.doFinal(block);
                decryptedData.append(new String(decryptedBlock, StandardCharsets.UTF_8));
            }

            return decryptedData.toString();
        } catch (Exception e) {
            log.error("RSA分段解密失败", e);
            throw new RuntimeException("RSA分段解密失败", e);
        }
    }

    /**
     * 从PEM格式的私钥中提取Base64编码的私钥
     *
     * @param pemPrivateKey PEM格式的私钥
     * @return Base64编码的私钥
     */
    public static String extractPrivateKeyFromPem(String pemPrivateKey) {
        if (pemPrivateKey == null || pemPrivateKey.trim().isEmpty()) {
            throw new IllegalArgumentException("私钥不能为空");
        }
        
        // 移除PEM格式的头部和尾部
        String key = pemPrivateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        return key;
    }

    /**
     * 从PEM格式的公钥中提取Base64编码的公钥
     *
     * @param pemPublicKey PEM格式的公钥
     * @return Base64编码的公钥
     */
    public static String extractPublicKeyFromPem(String pemPublicKey) {
        if (pemPublicKey == null || pemPublicKey.trim().isEmpty()) {
            throw new IllegalArgumentException("公钥不能为空");
        }
        
        // 移除PEM格式的头部和尾部
        String key = pemPublicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        return key;
    }

    /**
     * 生成RSA密钥对
     *
     * @param keySize 密钥长度（推荐2048）
     * @return 密钥对
     */
    public static KeyPair generateKeyPair(int keySize) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(keySize);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }

    /**
     * 将私钥转换为Base64编码的字符串
     *
     * @param privateKey 私钥
     * @return Base64编码的私钥
     */
    public static String privateKeyToBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 将公钥转换为Base64编码的字符串
     *
     * @param publicKey 公钥
     * @return Base64编码的公钥
     */
    public static String publicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
