package com.sdk.third.tf.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * TF Fiscal API签名工具类
 * 
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
public class SignUtil {

    /**
     * 生成签名
     * 签名算法：MD5(AppKey + Path + bodyString + timestamp)
     * 
     * @param appKey AppKey
     * @param path 请求路径
     * @param bodyString 请求体字符串
     * @param timestamp 时间戳（UTC）
     * @return MD5签名字符串（小写）
     */
    public static String generateSign(String appKey, String path, String bodyString, String timestamp) {
        if (CharSequenceUtil.isBlank(appKey)) {
            throw new IllegalArgumentException("AppKey不能为空");
        }
        if (CharSequenceUtil.isBlank(path)) {
            throw new IllegalArgumentException("Path不能为空");
        }
        if (bodyString == null) {
            bodyString = "";
        }
        if (CharSequenceUtil.isBlank(timestamp)) {
            throw new IllegalArgumentException("Timestamp不能为空");
        }

        // 拼接字符串：AppKey + Path + bodyString + timestamp
        String input = appKey + path + bodyString + timestamp;
        
        // 计算MD5
        String sign = DigestUtil.md5Hex(input);
        
        log.debug("生成签名, appKey: {}, path: {}, bodyLength: {}, timestamp: {}, sign: {}", 
            appKey, path, bodyString.length(), timestamp, sign);
        
        return sign;
    }

    /**
     * 生成UTC时间戳字符串
     * 
     * @return UTC时间戳字符串
     */
    public static String generateTimestamp() {
        // 获取当前UTC时间戳（秒）
        long timestamp = Instant.now().atZone(ZoneOffset.UTC).toEpochSecond();
        return String.valueOf(timestamp);
    }

    /**
     * 生成UTC时间戳字符串（毫秒）
     * 
     * @return UTC时间戳字符串（毫秒）
     */
    public static String generateTimestampMillis() {
        // 获取当前UTC时间戳（毫秒）
        long timestamp = Instant.now().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        return String.valueOf(timestamp);
    }
}
