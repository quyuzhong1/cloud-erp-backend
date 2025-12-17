package com.erp.server.wms.sign;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * HMAC、AES和MAC签名验签性能对比测试类
 * 
 * 测试内容：
 * 1. HMAC-SHA256 签名和验签性能
 * 2. AES-GCM 加密和解密性能  
 * 3. HMAC-MD5 签名和验签性能
 * 4. 不同数据量下的性能对比
 * 
 * @author ERP Team
 * @date 2024
 */
public class SignTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SignTest.class);
    
    // 测试配置
    private static final int WARMUP_ITERATIONS = 1000;
    private static final int TEST_ITERATIONS = 10000;
    private static final int[] DATA_SIZES = {64, 256, 1024, 4096, 16384}; // 不同数据大小（字节）
    
    // 密钥和算法配置
    private SecretKey hmacKey;
    private SecretKey aesKey;
    private SecretKey macKey;
    private SecureRandom secureRandom;
    
    @Before
    public void setUp() throws Exception {
        logger.info("开始初始化测试环境...");
        
        // 初始化随机数生成器
        secureRandom = new SecureRandom();
        
        // 生成HMAC密钥
        KeyGenerator hmacKeyGen = KeyGenerator.getInstance("HmacSHA256");
        hmacKey = hmacKeyGen.generateKey();
        
        // 生成AES密钥
        KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
        aesKeyGen.init(256);
        aesKey = aesKeyGen.generateKey();
        
        // 生成MAC密钥
        KeyGenerator macKeyGen = KeyGenerator.getInstance("HmacMD5");
        macKey = macKeyGen.generateKey();
        
        logger.info("测试环境初始化完成");
    }
    
    @After
    public void tearDown() {
        logger.info("测试环境清理完成");
    }
    
    /**
     * 生成指定大小的测试数据
     */
    private byte[] generateTestData(int size) {
        byte[] data = new byte[size];
        secureRandom.nextBytes(data);
        return data;
    }
    
    /**
     * HMAC-SHA256 签名
     */
    private byte[] hmacSign(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(hmacKey);
        return mac.doFinal(data);
    }
    
    /**
     * HMAC-SHA256 验签
     */
    private boolean hmacVerify(byte[] data, byte[] signature) throws Exception {
        byte[] expectedSignature = hmacSign(data);
        return MessageDigest.isEqual(signature, expectedSignature);
    }
    
    /**
     * AES-GCM 加密
     */
    private byte[] aesEncrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        
        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(data);
        
        // 将IV和加密数据组合
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        
        return result;
    }
    
    /**
     * AES-GCM 解密
     */
    private byte[] aesDecrypt(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        
        // 提取IV和加密数据
        byte[] iv = new byte[12]; // GCM IV通常是12字节
        byte[] encrypted = new byte[encryptedData.length - 12];
        System.arraycopy(encryptedData, 0, iv, 0, 12);
        System.arraycopy(encryptedData, 12, encrypted, 0, encrypted.length);
        
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);
        
        return cipher.doFinal(encrypted);
    }
    
    /**
     * HMAC-MD5 签名
     */
    private byte[] macSign(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(macKey);
        return mac.doFinal(data);
    }
    
    /**
     * HMAC-MD5 验签
     */
    private boolean macVerify(byte[] data, byte[] signature) throws Exception {
        byte[] expectedSignature = macSign(data);
        return MessageDigest.isEqual(signature, expectedSignature);
    }
    
    /**
     * 性能测试方法
     */
    private PerformanceResult runPerformanceTest(String algorithm, 
                                               TestOperation operation, 
                                               int dataSize, 
                                               int iterations) throws Exception {
        byte[] testData = generateTestData(dataSize);
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;
        
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            operation.execute(testData);
        }
        
        // 正式测试
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            operation.execute(testData);
            long endTime = System.nanoTime();
            
            long duration = endTime - startTime;
            totalTime += duration;
            minTime = Math.min(minTime, duration);
            maxTime = Math.max(maxTime, duration);
        }
        
        return new PerformanceResult(algorithm, dataSize, iterations, 
                                   totalTime, minTime, maxTime);
    }
    
    /**
     * 测试接口
     */
    @FunctionalInterface
    private interface TestOperation {
        void execute(byte[] data) throws Exception;
    }
    
    /**
     * 性能结果类
     */
    private static class PerformanceResult {
        private final String algorithm;
        private final int dataSize;
        private final int iterations;
        private final long totalTime;
        private final long minTime;
        private final long maxTime;
        
        public PerformanceResult(String algorithm, int dataSize, int iterations, 
                               long totalTime, long minTime, long maxTime) {
            this.algorithm = algorithm;
            this.dataSize = dataSize;
            this.iterations = iterations;
            this.totalTime = totalTime;
            this.minTime = minTime;
            this.maxTime = maxTime;
        }
        
        public double getAverageTimeMs() {
            return (totalTime / (double) iterations) / 1_000_000.0;
        }
        
        public double getMinTimeMs() {
            return minTime / 1_000_000.0;
        }
        
        public double getMaxTimeMs() {
            return maxTime / 1_000_000.0;
        }
        
        public double getThroughput() {
            return (iterations * dataSize) / (totalTime / 1_000_000_000.0) / 1024.0 / 1024.0; // MB/s
        }
        
        @Override
        public String toString() {
            return String.format("%s (数据大小: %d字节) - 平均: %.3fms, 最小: %.3fms, 最大: %.3fms, 吞吐量: %.2fMB/s",
                    algorithm, dataSize, getAverageTimeMs(), getMinTimeMs(), getMaxTimeMs(), getThroughput());
        }
    }
    
    /**
     * HMAC-SHA256 性能测试
     */
    @Test
    public void testHmacSha256Performance() throws Exception {
        logger.info("开始HMAC-SHA256性能测试...");
        
        for (int dataSize : DATA_SIZES) {
            // 签名性能测试
            PerformanceResult signResult = runPerformanceTest(
                "HMAC-SHA256-签名", 
                this::hmacSign, 
                dataSize, 
                TEST_ITERATIONS
            );
            logger.info("签名: {}", signResult);
            
            // 验签性能测试
            byte[] testData = generateTestData(dataSize);
            byte[] signature = hmacSign(testData);
            PerformanceResult verifyResult = runPerformanceTest(
                "HMAC-SHA256-验签", 
                data -> hmacVerify(data, signature), 
                dataSize, 
                TEST_ITERATIONS
            );
            logger.info("验签: {}", verifyResult);
        }
    }
    
    /**
     * AES-GCM 性能测试
     */
    @Test
    public void testAesGcmPerformance() throws Exception {
        logger.info("开始AES-GCM性能测试...");
        
        for (int dataSize : DATA_SIZES) {
            // 加密性能测试
            PerformanceResult encryptResult = runPerformanceTest(
                "AES-GCM-加密", 
                this::aesEncrypt, 
                dataSize, 
                TEST_ITERATIONS
            );
            logger.info("加密: {}", encryptResult);
            
            // 解密性能测试
            byte[] testData = generateTestData(dataSize);
            byte[] encryptedData = aesEncrypt(testData);
            PerformanceResult decryptResult = runPerformanceTest(
                "AES-GCM-解密", 
                data -> aesDecrypt(encryptedData), 
                dataSize, 
                TEST_ITERATIONS
            );
            logger.info("解密: {}", decryptResult);
        }
    }
    
    /**
     * HMAC-MD5 性能测试
     */
    @Test
    public void testHmacMd5Performance() throws Exception {
        logger.info("开始HMAC-MD5性能测试...");
        
        for (int dataSize : DATA_SIZES) {
            // 签名性能测试
            PerformanceResult signResult = runPerformanceTest(
                "HMAC-MD5-签名", 
                this::macSign, 
                dataSize, 
                TEST_ITERATIONS
            );
            logger.info("签名: {}", signResult);
            
            // 验签性能测试
            byte[] testData = generateTestData(dataSize);
            byte[] signature = macSign(testData);
            PerformanceResult verifyResult = runPerformanceTest(
                "HMAC-MD5-验签", 
                data -> macVerify(data, signature), 
                dataSize, 
                TEST_ITERATIONS
            );
            logger.info("验签: {}", verifyResult);
        }
    }
    
    /**
     * 综合性能对比测试
     */
    @Test
    public void testComprehensivePerformanceComparison() throws Exception {
        logger.info("开始综合性能对比测试...");
        
        int testDataSize = 1024; // 使用1KB数据进行对比
        int testIterations = 5000;
        
        // HMAC-SHA256 测试
        PerformanceResult hmacSignResult = runPerformanceTest(
            "HMAC-SHA256-签名", this::hmacSign, testDataSize, testIterations);
        PerformanceResult hmacVerifyResult = runPerformanceTest(
            "HMAC-SHA256-验签", 
            data -> hmacVerify(data, hmacSign(data)), testDataSize, testIterations);
        
        // AES-GCM 测试
        PerformanceResult aesEncryptResult = runPerformanceTest(
            "AES-GCM-加密", this::aesEncrypt, testDataSize, testIterations);
        PerformanceResult aesDecryptResult = runPerformanceTest(
            "AES-GCM-解密", 
            data -> aesDecrypt(aesEncrypt(data)), testDataSize, testIterations);
        
        // HMAC-MD5 测试
        PerformanceResult macSignResult = runPerformanceTest(
            "HMAC-MD5-签名", this::macSign, testDataSize, testIterations);
        PerformanceResult macVerifyResult = runPerformanceTest(
            "HMAC-MD5-验签", 
            data -> macVerify(data, macSign(data)), testDataSize, testIterations);
        
        // 输出对比结果
        logger.info("=== 性能对比结果 (数据大小: {}字节, 迭代次数: {}) ===", testDataSize, testIterations);
        logger.info("签名算法性能对比:");
        logger.info("  {}", hmacSignResult);
        logger.info("  {}", macSignResult);
        logger.info("验签算法性能对比:");
        logger.info("  {}", hmacVerifyResult);
        logger.info("  {}", macVerifyResult);
        logger.info("加密算法性能对比:");
        logger.info("  {}", aesEncryptResult);
        logger.info("解密算法性能对比:");
        logger.info("  {}", aesDecryptResult);
        
        // 计算相对性能
        logger.info("=== 相对性能对比 ===");
        double hmacSignTime = hmacSignResult.getAverageTimeMs();
        double macSignTime = macSignResult.getAverageTimeMs();
        double aesEncryptTime = aesEncryptResult.getAverageTimeMs();
        
        logger.info("HMAC-MD5签名速度是HMAC-SHA256的 {} 倍", hmacSignTime / macSignTime);
        logger.info("AES-GCM加密时间是HMAC-SHA256签名的 {} 倍", aesEncryptTime / hmacSignTime);
    }
    
    /**
     * 功能正确性验证测试
     */
    @Test
    public void testFunctionalityCorrectness() throws Exception {
        logger.info("开始功能正确性验证测试...");
        
        String testMessage = "这是一个用于测试签名和加密功能正确性的消息";
        byte[] testData = testMessage.getBytes(StandardCharsets.UTF_8);
        
        // HMAC-SHA256 功能测试
        byte[] hmacSignature = hmacSign(testData);
        boolean hmacValid = hmacVerify(testData, hmacSignature);
        logger.info("HMAC-SHA256 功能测试: {}", hmacValid ? "通过" : "失败");
        
        // AES-GCM 功能测试
        byte[] encryptedData = aesEncrypt(testData);
        byte[] decryptedData = aesDecrypt(encryptedData);
        boolean aesValid = MessageDigest.isEqual(testData, decryptedData);
        logger.info("AES-GCM 功能测试: {}", aesValid ? "通过" : "失败");
        
        // HMAC-MD5 功能测试
        byte[] macSignature = macSign(testData);
        boolean macValid = macVerify(testData, macSignature);
        logger.info("HMAC-MD5 功能测试: {}", macValid ? "通过" : "失败");
        
        // 验证所有测试都通过
        assert hmacValid : "HMAC-SHA256 功能测试失败";
        assert aesValid : "AES-GCM 功能测试失败";
        assert macValid : "HMAC-MD5 功能测试失败";
        
        logger.info("所有功能正确性测试通过！");
    }
}
