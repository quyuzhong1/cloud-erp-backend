package com.common.business.constant;

/**
 * Token的Key常量
 *
 * @author ruoyi
 */
public class TokenConstants {
    /**
     * 令牌自定义标识
     */
    public static final String AUTHENTICATION = "Authorization";

    /**
     * 网关注入的登录用户上下文
     */
    public static final String TOKEN_USER_INFO = "tokenUserInfo";

    /**
     * API Token认证通过后的令牌ID，仅用于审计、幂等等内部识别，不是认证凭证
     */
    public static final String API_TOKEN_ID_HEADER = "X-Erp-Api-Token-Id";

    /**
     * 令牌前缀
     */
    public static final String PREFIX = "Bearer ";

    /**
     * 令牌秘钥
     */
    public final static String SECRET = "abcdefghijklmnopqrstuvwxyz";

}
