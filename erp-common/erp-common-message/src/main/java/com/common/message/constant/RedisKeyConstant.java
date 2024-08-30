package com.common.message.constant;

public class RedisKeyConstant {
    public static final String SKU_LISTING_TIME = "SKU_LISTING_TIME";

    public static final String SKU_NOT_LISTING_TIME = "SKU_NOT_LISTING_TIME";
    public static final String MABANG_STOCK_SKU_LIST_KEY = "SKU:MABANG_STOCK_SKU_KEY";

    public static final String MABANG_FINANCIAL_SKU_LIST_KEY = "SKU:MABANG_FINANCIAL_SKU_KEY";
    public static final String LIST_SKU_INFO = "SKU:LIST_SKU_INFO";

    /**
     * 不需要扣减库存的sku ，服务类，费用类
     */
    public static final String CACHE_SKU_NO_INVENTORY = "cache:plm:getNoInventorySku";

    //催办
    public static String PRESS="_PRESS";

    /**
     *  销售出库金蝶
     */
    public static String KINGDEE_XSCK="KINGDEE_XSCK:{}";

    /**
     * 库存锁定无法操作
     * 计划单号 + 组织 + 仓库 + 库位 + sku + 状态
     */
    public static String INVENTORY_LOCK="INVENTORY_LOCK:{}_{}_{}_{}_{}_{}";

    /**
     * 库存锁定无法操作
     * 计划单号
     */
    public static String INVENTORY_LOCK_CODE="INVENTORY_LOCK:{}_*";

    /**
     * SKU含税成本
     * skuNo
     */
    public static String DMP_SKU_COST_CODE = "DMP_SKU_COST:{}_*";

    public static String WMS_PACKING_INSPECTION = "WMS:PACKING_INSPECTION:{}";

    /**
     * 拉取任务预警redis的key
     */
    public static String DMP_PUSH_TASK_WARN = "DMP_PUSH_TASK_WARN:{}";

    /**
     * 推送任务预警redis的key
     */
    public static String DMP_PULL_TASK_WARN = "DMP_PULL_TASK_WARN:{}";

    /**
     * 库存锁定无法操作
     * 计划单号
     */
    public static String SKU_OCCUPY_CODE="SKU_OCCUPY_CODE:{}_{}";

    /**
     * 结算汇率缓存,目标币别+原币别
     */
    public static String SETTLEMENT_EXCHANGE_RATE = "SETTLEMENT_EXCHANGE_RATE:{}_{}";

    public static final String SO_B2C_ORDER_KEY = "SO_B2C_ORDER_KEY";
    public static final String SO_B2C_DELIVERY_KEY = "SO_B2C_DELIVERY_KEY";


    /**
     * 生成销售出库单key
     */
    public static final String SO_STOCK_KEY = "SO_STOCK_KEY";
}
