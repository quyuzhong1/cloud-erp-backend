package com.common.core.constant;

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

    //邮箱验证码
    String CODE_OF_EMAIL = "email_code_";

    /**
     * 邮箱验证码 有效期间5分钟
     */
    public long EMAIL_CODE_EXPIRATION = 5;


    /**
     * 权限功能的redis 的key
     */
    String PERMISSIONS_CODE_KEY = "permissions_code:";

}
