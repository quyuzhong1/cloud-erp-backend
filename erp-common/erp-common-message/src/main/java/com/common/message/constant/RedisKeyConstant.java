package com.common.message.constant;

public class RedisKeyConstant {
    public static final String SKU_LISTING_TIME = "SKU_LISTING_TIME";

    public static final String SKU_NOT_LISTING_TIME = "SKU_NOT_LISTING_TIME";

    //催办
    public static String PRESS="_PRESS";

    /**
     *  销售出库金蝶
     */
    public static String KINGDEE_XSCK="KINGDEE_XSCK:{}";

    /**
     * 库存锁定无法操作
     * 单号 + 组织 + 仓库 + 库位 + sku + 状态
     */
    public static String INVENTORY_LOCK="INVENTORY_LOCK:{}_{}_{}_{}_{}_{}";

    /**
     * 库存锁定无法操作
     * 单号
     */
    public static String INVENTORY_LOCK_CODE="INVENTORY_LOCK:{}_*";

}
