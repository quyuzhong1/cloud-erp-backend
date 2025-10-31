package com.erp.server.auth.test;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
/**
 * 生成appID和appSecret的方法
 *
 * @author wuhaotian
 * @since 2025-09-18
 */

public class AppKeyGenerator {

    // 生成 appId（UUID 去掉短横线）
    public static String generateAppId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 生成 appSecret（32字节 -> Base64 字符串，大约44位）
    public static String generateAppSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256位
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static void main(String[] args) {
        System.out.println("appId: " + generateAppId());
        System.out.println("appSecret: " + generateAppSecret());
    }
}
