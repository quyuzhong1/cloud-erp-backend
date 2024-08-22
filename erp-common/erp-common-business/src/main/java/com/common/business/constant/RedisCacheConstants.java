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

    /**
     *  邮箱验证码
     */

    String CODE_OF_EMAIL = "email_code_";

    /**
     * 邮箱验证码 有效期间5分钟
     */
    public long EMAIL_CODE_EXPIRATION = 5;

    /**
     * 登录错误限制次数
     */
    public final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 登录错误限制间隔分钟
     */
    public final int LOCK_DURATION_MINUTES = 60;

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
     * 登录错误Key : 系统:用户账号
     */
    String LOGIN_ERROR_KEY = "login_error:{}:{}";

    /**
     * 亚马逊报告文档URL:{文档类型}:MarketplaceId
     */
    String REDIS_AMAZON_REPORT_DOCUMENT_URL = "amazon-report-document-{}-{}";



    // 授权相关
    /**
     * 邮箱验证码 有效期间600秒
     */
     Integer THIRD_PARTY_AUTH_EXPIRATION = 600;

    /**
     * 亚马逊授权:{state}
     * 内容店铺ID
     */
    String AUTH_AMAZON_STATE = "third-party-auth:amazon_state:{}";

    /**
     * shopify授权:{shop}
     * 内容店铺ID
     */
    String AUTH_SHOPIFY_SHOP = "third_party_auth:shopify_shop:{}";

    /**
     * 速卖通授权:{stare}
     * 内容店铺ID
     */
    String AUTH_ALIEXPRESS_STATE = "third-party-auth:aliexpress_state:{}";

    /**
     * 虾皮授权:{id}
     * 内容店铺ID
     */
    String AUTH_SHOPEE_ID = "third-party-auth:id:{}";

    /**
     * 亚马逊RDT token:店铺ID:订单ID
     */
    String AMAZON_RDT_TOKEN = "amazon-rdt-token:{}:{}";


    /**
     * 平台请求中:平台类型:sellerId:业务类型/接口类型
     * :请求的端点区域?
     */
    String PLATFORM_REQUEST = "platform_request:{}:{}:{}";


    /**
     * 平台请求中前缀:groupId
     */
    String PLATFORM_REQUEST_PREFIX = "platform_request:{}";

    /**
     * 亚马逊报告同类型处理中:shopId:recordType
     */
    String AMZ_REPORT_HANDLE_PREFIX = "amz_report_handle:{}:{}";

    /**
     * 亚马逊创建报告缓存响应信息:amz_report_result:taskId:status
     */
    String AMZ_REPORT_RESULT_PREFIX = "amz_report_result:{}:{}";


    /**
     * 亚马逊报告缓存结果:amz_report_info:taskId:status
     */
    String AMZ_REPORT_INFO_PREFIX = "amz_report_info:{}:{}";

    /**
     * 平台请求频率:平台类型:sellerId:业务类型/接口类型
     */
    String PLATFORM_RATE_LIMIT = "platform_rate_limit:{}:{}:{}";

    /**
     * 平台请求频率:平台类型:sellerId:业务类型/接口类型:端点
     */
    String PLATFORM_RATE_LIMIT_ENDPOINTS = "platform_rate_limit:{}:{}:{}:{}";

    /**
     * 平台请求频率:{(平台类型:sellerId::端点)=groupId}:{业务类型/接口类型}
     */
    String PLATFORM_RATE_LIMIT_GROUP_ID_PREFIX = "platform_rate_limit:{}:{}";


    /**
     * 平台请求频率前缀:groupId
     */
    String PLATFORM_RATE_LIMIT_PREFIX = "platform_rate_limit:{}";

    /**
     * 美客多授权:{id}
     * 内容店铺ID
     */
    String AUTH_MERCADO_STATE = "third-party-auth:mercado_state:{}";

    /**
     * TikTok授权:{id}
     * 内容店铺ID
     */
    String AUTH_TIKTOK_STATE = "third-party-auth:tiktok_state:{}";

    /**
     * 平台token刷新重试次数记录
     * platform-refresh-token:平台名称:店铺ID
     */
    String REDIS_REFRESH_PLATFORM_TOKEN = "platform-refresh-token:{}:{}";


    /**
     * 平台请求频率:groupId:操作类型
     */
    String PLATFORM_RATE_LIMIT_PREFIX_LAST = "platform_rate_limit:{}:{}";


    /**
     * 亚马逊接口请求缓存响应信息:amz_sp_api_result:businessTypeName:请求的唯一key
     */
    String AMZ_SP_API_RESULT_PREFIX = "amz_sp_api_result:{}:{}";


    /**
     * 国家对应时区配置:cfg_timezone_prefix:国家代号
     */
    String CFG_TIMEZONE_PREFIX = "cfg_timezone:{}";

    /**
     * 国家对应时区配置
     */
    String CFG_TIMEZONE = "cfg_timezone";

    /**
     * 物流报关单合同号
     */
    String TMS_DECLARE_CODE = "tms_declare_code:{}_{}";

    /**
     * 亚马逊订单任务开始时间前置:taskId
     */
    String AMAZON_ORDER_TASK_TIME_PREFIX = "amazon_order_start_time:{}";

    String SNOWFLAKE_KEY = "snowflake_key:{}";


    /**
     * 组包标记发货key:{平台}:{店铺ID}
     */
    String MERGE_PACKAGE_SIGN_DELIVERY_KEY = "merge_package_sign_delivery_key:{}:{}";


    /**
     * 组包生成销售出库单扣库存key:{扣库存key}
     */
    String MERGE_PACKAGE_INVENTORY_KEY = "merge_package_inventory_key:{}";


    /**
     * 组包消费重试次数:{soId}
     */
    String MERGE_PACKAGE_RETRY_COUNT_KEY = "merge_package_retry_count_key:{}";



    /**
     * 中台拉取track123标记
     */
    String DMP_TRACK123_TRACK_NO = "dmp_track123_track_no";

}
