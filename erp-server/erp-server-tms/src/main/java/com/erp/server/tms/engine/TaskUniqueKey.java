package com.erp.server.tms.engine;

import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 任务防重键值对象，由 TmsAsyncTaskUniqueKeyGenerator 构造。
 * uniqueKey 为 uniqueKeyText 的 MD5 hash（32 位十六进制），用于数据库唯一索引。
 * uniqueKeyText 为可读防重原文，用于排查问题。
 */
@Getter
public final class TaskUniqueKey {

    private static final String DEFAULT_TEXT = "default";

    private final String uniqueKey;
    private final String uniqueKeyText;

    private TaskUniqueKey(String uniqueKeyText) {
        this.uniqueKeyText = uniqueKeyText;
        this.uniqueKey = md5(uniqueKeyText);
    }

    /**
     * 无额外防重参数时使用默认唯一键
     */
    public static TaskUniqueKey ofDefault() {
        return new TaskUniqueKey(DEFAULT_TEXT);
    }

    /**
     * 使用已归一化的防重原文构造唯一键
     */
    public static TaskUniqueKey of(String uniqueKeyText) {
        if (uniqueKeyText == null || uniqueKeyText.isEmpty()) {
            return ofDefault();
        }
        return new TaskUniqueKey(uniqueKeyText);
    }

    private static String md5(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 is guaranteed present in JVM; never reaches here
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    @Override
    public String toString() {
        return "TaskUniqueKey{uniqueKey='" + uniqueKey + "', uniqueKeyText='" + uniqueKeyText + "'}";
    }
}
