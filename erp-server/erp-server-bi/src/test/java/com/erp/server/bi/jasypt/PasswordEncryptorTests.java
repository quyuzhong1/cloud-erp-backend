package com.erp.server.bi.jasypt;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 *
 * @Author Cloud
 * @Date 2023/1/3 17:25
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class PasswordEncryptorTests {

    @Autowired
    private StringEncryptor stringEncryptor;
    /**
     * 加密解密测试
     */
    @Test
    public void jasyptTest() {
        // 加密
        String encrypted = stringEncryptor.encrypt("admin@viji");
        System.out.println(encrypted);
        // 你可以在这里添加加密结果的断言，假设期望的加密值是 expectedEncryptedValue
        String expectedEncryptedValue = "iEu7/GU6+IlQ634RQ89l4j5aFNklnbxnTEPxEeAmJ8VuJBT/2qDYIz3x8cTTPc70"; // 示例预期值
        assertEquals("加密后的值不匹配", expectedEncryptedValue, encrypted);

        // 解密
        String decrypted = stringEncryptor.decrypt(encrypted);
        System.out.println(decrypted);
        // 断言解密后的值是否符合预期
        assertEquals("解密后的值不匹配", "admin@viji", decrypted);
    }

    /**
     * 手动测试
     */
    @Test
    public void test() {
        // 设置加密配置
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("UlanZi_jaspyt_password");
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);

        // 加密
        String encrypted = encryptor.encrypt("admin@viji");
        System.out.println("Encrypted: " + encrypted);

        // 断言加密后的结果不为空
        assertNotNull("加密后的结果不应为空", encrypted);

        // 你也可以根据预期的加密值来进行验证（如果已知加密值）
        String expectedEncryptedValue = "iEu7/GU6+IlQ634RQ89l4j5aFNklnbxnTEPxEeAmJ8VuJBT/2qDYIz3x8cTTPc70"; // 示例预期值
        assertEquals("加密后的结果不符合预期", expectedEncryptedValue, encrypted);

        // 解密
        String decrypted = encryptor.decrypt(encrypted);
        System.out.println("Decrypted: " + decrypted);

        // 断言解密后的结果是原始值
        assertEquals("解密后的结果不符合预期", "admin@viji", decrypted);
    }

}
