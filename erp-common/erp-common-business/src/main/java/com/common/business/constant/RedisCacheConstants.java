package com.common.business.constant;

/**
 * @Classname PrefixOfCacheKey

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

    /**
     * wms dic 的 key
     */
    String WMS_DICT_KEY = "wms_dict";

    /**
     * pda用户叉掉消息通知的 key
     */
    String CLOSE_MESSAGE_NOTICE_KEY = "close_message_notice:";

    /**
     * 平台token
     * platform-token:平台名称:店铺ID
     */
    String REDIS_PLATFORM_TOKEN = "platform-token:{}:{}";

    /**
     * 亚马逊报告文档URL:MarketplaceId
     */
    String REDIS_AMAZON_REPORT_DOCUMENT_URL = "amazon-report-document-{}";
}
