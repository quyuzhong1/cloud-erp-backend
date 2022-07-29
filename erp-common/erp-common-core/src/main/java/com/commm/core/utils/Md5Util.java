package com.commm.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @Classname Md5Util MD5 工具
 * @Description TODO
 * @Date 2022-07-06 11:51
 * @Created by yl
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
            return null;
        }
    }
}
