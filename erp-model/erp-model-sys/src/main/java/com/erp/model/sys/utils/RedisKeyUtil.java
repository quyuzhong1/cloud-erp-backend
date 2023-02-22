package com.erp.model.sys.utils;

import com.common.core.utils.Md5Util;
import com.common.business.constant.RedisCacheConstants;

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
