package com.comm.core.constant;

/**
 * @Classname PrefixOfCacheKey
 * @Description TODO
 * @Date 2022-07-14 15:24
 * @Created by yl
 */
public interface RedisCacheConstants {


    String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 缓存有效期，默认7 天
     */
    public long EXPIRATION = 7;


}
