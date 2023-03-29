package com.common.core.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import cn.hutool.core.math.MathUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author yl
 * @Classname Md5Util MD5 工具
 * @Description TODO
 * @Date 2022-07-06 11:51
 */
public class Md5Util {

    /**
     * 简单MD5
     *
     * @param str
     * @return
     */
    public static String md5(String str) {

        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] array = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
