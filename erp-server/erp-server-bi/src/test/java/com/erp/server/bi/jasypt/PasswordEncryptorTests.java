package com.erp.server.bi.jasypt;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TODO
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
        System.out.println(stringEncryptor.encrypt("admin@viji"));    // iEu7/GU6+IlQ634RQ89l4j5aFNklnbxnTEPxEeAmJ8VuJBT/2qDYIz3x8cTTPc70
        // 解密
        System.out.println(stringEncryptor.decrypt("iEu7/GU6+IlQ634RQ89l4j5aFNklnbxnTEPxEeAmJ8VuJBT/2qDYIz3x8cTTPc70"));    // root
    }

    /**
     * 手动测试
     */
    @Test
    public void test() {
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
        System.out.println(encryptor.encrypt("admin@viji"));    // iEu7/GU6+IlQ634RQ89l4j5aFNklnbxnTEPxEeAmJ8VuJBT/2qDYIz3x8cTTPc70
    }

}
