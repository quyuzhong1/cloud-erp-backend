package com.common.business.constant;

import cn.hutool.core.util.StrUtil;

/**
 * @Classname PrefixOfCacheKey

 * @Date 2022-07-14 15:24
 * @Created by yl
 */
public interface RedisCacheConstants {
    /**
     * 缓存有效期，默认7 天
     */
    long EXPIRATION = 7;

    /**
     * 邮箱验证码 有效期间5分钟
     */
    long EMAIL_CODE_EXPIRATION = 5;

    /**
     * 登录错误限制次数
     */
    int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 登录错误限制间隔分钟
     */
    int LOCK_DURATION_MINUTES = 60;

    /**
     * 邮箱验证码 有效期间600秒
     */
    Integer THIRD_PARTY_AUTH_EXPIRATION = 600;


    String LOGIN_TOKEN_KEY = "auth:login:token:";

    /**
     *  邮箱验证码
     */

    String CODE_OF_EMAIL = "auth:login:email:code";



    /**
     * pda用户叉掉消息通知的 key
     */
    String CLOSE_MESSAGE_NOTICE_KEY = "pda:message:closeNotice:";

    /**
     * 平台token
     * platform-token:平台名称:店铺ID
     */
    String REDIS_PLATFORM_TOKEN = "platform-token:{}:{}";
    /**
     * 登录错误Key : 系统:用户账号
     */
    String LOGIN_ERROR_KEY = "auth:login:error:{}:{}";



    // 授权相关

    /**
     * 亚马逊授权:{state}
     * 内容店铺ID
     */
    String AUTH_AMAZON_STATE = "third:amazon:auth:state:{}";

    /**
     * shopify授权:{shop}
     * 内容店铺ID
     */
    String AUTH_SHOPIFY_SHOP = "third:shopify:auth:state:{}";

    /**
     * 速卖通授权:{stare}
     * 内容店铺ID
     */
    String AUTH_ALIEXPRESS_STATE = "third:aliexpress:auth:state:{}";

    /**
     * 虾皮授权:{id}
     * 内容店铺ID
     */
    String AUTH_SHOPEE_ID = "third:shopee:auth:id:{}";

    /**
     * 亚马逊RDT token:店铺ID:订单ID
     */
    String AMAZON_RDT_TOKEN = "third:amazon:rdt:token:{}:{}";

    /**
     * 亚马逊报告同类型处理中:shopId:recordType
     */
    String AMZ_REPORT_HANDLE_PREFIX = "third:amazon:report:handle:{}:{}";

    /**
     * 亚马逊创建报告缓存响应信息:amz_report_result:taskId:status
     */
    String AMZ_REPORT_RESULT_PREFIX = "third:amazon:report:result:{}:{}";


    /**
     * 亚马逊报告缓存结果:amz_report_info:taskId:status
     */
    String AMZ_REPORT_INFO_PREFIX = "third:amazon:report:info:{}:{}";

    /**
     * 平台请求频率:平台类型:sellerId:业务类型/接口类型
     */
    String PLATFORM_RATE_LIMIT = "third:platform:rate:limit:{}:{}:{}";


    /**
     * 平台请求频率:{(平台类型:sellerId::端点)=groupId}:{业务类型/接口类型}
     */
    String PLATFORM_RATE_LIMIT_GROUP_ID_PREFIX = "third:platform:rate:limit:{}:{}";


    /**
     * 平台请求频率前缀:groupId
     */
    String PLATFORM_RATE_LIMIT_PREFIX = "third:platform:rate:limit:{}";

    /**
     * 美客多授权:{id}
     * 内容店铺ID
     */
    String AUTH_MERCADO_STATE = "third:mercado:auth:state:{}";

    /**
     * TikTok授权:{id}
     * 内容店铺ID
     */
    String AUTH_TIKTOK_STATE = "third:tiktok:auth:state:{}";

    String AUTH_PDD_STATE = "third:pdd:auth:state:{}";
    /**
     * 平台token刷新重试次数记录
     * platform-refresh-token:平台名称:店铺ID
     */
    String REDIS_REFRESH_PLATFORM_TOKEN = "third:platform:refreshToken:{}:{}";


    /**
     * 平台请求频率:groupId:操作类型
     */
    String PLATFORM_RATE_LIMIT_PREFIX_LAST = "third:platform:rate:limit:{}:{}";


    /**
     * 亚马逊接口请求缓存响应信息:amz_sp_api_result:businessTypeName:请求的唯一key
     */
    String AMZ_SP_API_RESULT_PREFIX = "third:amazon:sp:apiResult:{}:{}";


    /**
     * 国家对应时区配置:cfg_timezone_prefix:国家代号
     */
    String CFG_TIMEZONE_PREFIX = "dmp:config:timezone:{}";

    /**
     * 物流报关单合同号
     */
    String TMS_DECLARE_CODE = "tms:declare:code:{}_{}";

    /**
     * 亚马逊订单任务开始时间前置:taskId
     */
    String AMAZON_ORDER_TASK_TIME_PREFIX = "third:amazon:task:startTime:{}";

    /**
     * 雪花算法key
     */
    String SNOWFLAKE_KEY = "snowflake:key:{}";


    /**
     * 组包标记发货key:{平台}:{店铺ID}
     */
    String MERGE_PACKAGE_SIGN_DELIVERY_KEY = "wms:mergePackage:signDelivery:{}:{}";


    /**
     * 组包生成销售出库单扣库存key:{扣库存key}
     */
    String MERGE_PACKAGE_INVENTORY_KEY = "wms:mergePackage:inventory:{}";


    /**
     * 组包消费重试次数:{soId}
     */
    String MERGE_PACKAGE_RETRY_COUNT_KEY = "wms:mergePackage:retryCount:{}";

    /**
     * B2C 发货单生成直接调拨单互斥锁:{发货单id}
     * 多个并发入口（组包 MQ、重新出库、intercept、async 等）调用 pushTransferInfoError 时按发货单串行，
     * 防止 check-then-act + Seata XA 提交窗口内并发导致重复生成 transfer_info
     */
    String SO_B2C_DELIVERY_PUSH_TRANSFER_INFO_LOCK = "wms:soB2cDelivery:pushTransferInfo:{}";

    /**
     * 中台拉取track123海运标记
     */
    String DMP_TRACK123_TRACK_OCEAN_LOGISTICS_NO = "dmp:track123:ocean:trackNo";

    /**
     * 中台历史输出记录总数
     */
    String DMP_OUTPUT_RECORD_HIS_COUNT = "dmp:output:record:his:count";


    /**
     * 飞书接口请求缓存响应信息:fei_shu_api_result:businessTypeName:请求的唯一key
     */
    String FEI_SHU_RESULT_PREFIX = "third:feishu:apiResult:{}:{}";


    String TMS_LOGISTIC_LABEL = "tms:logistic:label:{}:{}";

    String SKU_LISTING_TIME = "plm:sku:listingTime";
    String MABANG_STOCK_SKU_LIST_KEY = "third:mabang:stock:sku";

    String MABANG_FINANCIAL_SKU_LIST_KEY = "third:mabang:financial:sku";
    String LIST_SKU_INFO = "plm:sku:info";

    /**
    *ff飞书催办消息key前缀:third:feishu:msg:业务类型:业务ID:催办
     */
    String FEISHU_REDIS_KEY_PREFIX="third:feishu:msg:";

    /**
     * 库存锁定无法操作
     * 计划单号 + 组织 + 仓库 + 库位 + sku + 状态
     */
    String INVENTORY_LOCK="lock:wms:inventory:{}_{}_{}_{}_{}_{}";

    /**
     * 库存锁定无法操作
     * 计划单号
     */
    String INVENTORY_LOCK_CODE="lock:wms:inventory:{}_*";

    /**
     * SKU含税成本
     * skuNo
     */
    String DMP_SKU_COST_CODE = "dmp:sku:cost:{}_*";

    String WMS_PACKING_INSPECTION = "wms:packing:inspection:{}";

    /**
     * 拉取任务预警redis的key
     */
    String DMP_PUSH_TASK_WARN = "dmp:push:task:warn:{}";

    /**
     * 推送任务预警redis的key
     */
    String DMP_PULL_TASK_WARN = "dmp:pull:task:warn:{}";

    /**
     * 库存锁定无法操作
     * 计划单号
     */
    String SKU_OCCUPY_CODE="plm:sku:occupy:{}_{}";

    /**
     * 结算汇率缓存,目标币别+原币别
     */
    String SETTLEMENT_EXCHANGE_RATE = "dmp:settlement:exchangeRate:{}_{}";


    /**
     * 生成销售出库单key
     */
    String SO_STOCK_KEY = "wms:order:stock:add";

    /**
     * 更新产品上架时间
     */
    String PRODUCT_LISTING_TIME = "plm:product:listingTime:";

    /**
     * 重试任务key
     */
    String SOB2C_RETRY_JOB = "oms:b2c:retry:job:{}";

    /**
     * 虚拟仓报表数据缓存
     */
    String REPORT_VIRTUAL_ORDER_DATA = "wms:virtual:order:data";
    //sso Redis键值对存储对称密钥
    String SSO_SIGN_SESSION = "auth:sso:signSession:";

    String REDIS_GEN_KEY = "snowflake:order:code";

    String MRP_DATA_ARCHIVING_KEY = "mrp:data:archiving";
    String MRP_KEY = "mrp";

    String LOCK_KEY_PREFIX = "wms:fbt:sync:";

    String DATA_COMPARE_TASK_KEY = "wms:data:compare:task:";

    String WEB_VERSION_REDISKEY = "web:version:package";
    String IDEM_REDISKEY = "idem:";
    String DATA_IDEM_REDISKEY = "idem:data:";

    String TABLE_BUSINESS_KEY = "sys:table:business:key";

    String WECHAT_ACCESS_TOKEN_KEY = "third:wechat:access:token";

    String ADD_GYY_REFUND_ORDER_KEY = "dmp:gyy:refund:add";
    String ADD_GYY_RETURN_ORDER_KEY = "dmp:gyy:return:add";

    String WDT_ERROR_CODE_KEY = "dmp:wdt:error:code:";
    String SO_B2C_NOT_OUTBOUND_KEY = "oms:b2c:notbound";

    /**
     * 系统通知SSE在线节点:应用端
     */
    String SYS_NOTICE_SSE_ONLINE_NODE = "sys:notice:sseNode:{}";

    /**
     * 系统通知SSE用户在线节点路由:应用端
     */
    String SYS_NOTICE_SSE_USER_NODE = "sys:notice:sseUserNode:{}";

    /**
     * 系统通知SSE用户路由锁:应用端_用户ID
     */
    String SYS_NOTICE_SSE_USER_LOCK = "sys:notice:sseUserLock:{}_{}";

    /**
     * 系统通知SSE节点topic:应用端_节点ID
     */
    String SYS_NOTICE_SSE_NODE_TOPIC = "sys:notice:sseTopic:{}_{}";

    /**
     * 系统消息分发延迟队列
     */
    String SYS_MESSAGE_DISPATCH_DELAY_QUEUE = "sys:message:dispatchDelay:queue";

    /**
     * 系统消息分发延迟队列去重标记
     */
    String SYS_MESSAGE_DISPATCH_DELAY_QUEUED = "sys:message:dispatchDelay:queued";

    static String buildSysNoticeSseOnlineNodeKey(String application) {
        return StrUtil.format(SYS_NOTICE_SSE_ONLINE_NODE, application);
    }

    static String buildSysNoticeSseUserNodeKey(String application) {
        return StrUtil.format(SYS_NOTICE_SSE_USER_NODE, application);
    }

    static String buildSysNoticeSseUserLockKey(String application, String userId) {
        return StrUtil.format(SYS_NOTICE_SSE_USER_LOCK, application, userId);
    }

    static String buildSysNoticeSseNodeTopic(String application, String nodeId) {
        return StrUtil.format(SYS_NOTICE_SSE_NODE_TOPIC, application, nodeId);
    }
    String SO_B2C_DELIVERY_WITH_NOT_OUTBOUND_KEY = "oms:b2c:deliveryWithNotOutbound:";

    /**
     * 动态数据源 Doris 路由配置全量刷新广播 channel
     * 由 DMP 进程在 cfg_setting(type=doris_query_cfg) 重建本地缓存后 publish；
     * 各业务节点订阅后原子替换本地全量快照
     */
    String DORIS_QUERY_CFG_REFRESH_CHANNEL = "erp:doris_query_cfg:refresh";

    /**
     * 动态数据源 Doris 路由配置全量持久化 key
     * DMP 每次广播前先写入此 Bucket（持久化全量 + version），业务节点启动时 @PostConstruct
     * 直接读取避免冷启动空窗（与周期广播互补，遵循"先写 Bucket 再 publish"的写入顺序）
     */
    String DORIS_QUERY_CFG_FULL_KEY = "erp:doris_query_cfg:full";
}
