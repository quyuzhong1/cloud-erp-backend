package com.common.core.utils;

import com.common.core.constant.RedisCacheConstants;

/**
 * @Classname RedisKeyUtil
 * @Description TODO
 * @Date 2022-08-02 12:05
 * @Created by yl
 */
public class RedisKeyUtil {

    /**
     *  获取邮箱验证码 的key
     *
     * @param email 邮件地址
     * @return
     */
    public static String getEmailCodeCacheKey(String email) {
        return new StringBuffer(RedisCacheConstants.CODE_OF_EMAIL).append(Md5Util.md5(email)).toString();
    }
}
