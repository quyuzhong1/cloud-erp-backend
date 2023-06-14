package com.erp.model.dmp.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface MongoTableNameContant {

    /**
     * 马帮原始数据表名称
     */
    /**
     * 订单表
     */
    String ORIGINAL_MABANG_ORDER = "original_mabang_order";

    /**
     * 商品表
     */
    String ORIGINAL_MABANG_SKU = "original_mabang_sku";

    /**
     * 退货订单表
     */
    String ORIGINAL_MABANG_RETURN_ORDER = "original_mabang_return_order";

    /**
     * 退款数据
     */
    String ORIGINAL_MABANG_REFUND = "original_mabang_refund";


    /**
     * 店铺数据
     */
    String ORIGINAL_MABANG_SHOP = "original_mabang_shop";


    /**
     * 出库详情
     */
    String ORIGINAL_MABANG_DELIVERY_DETAIL = "original_mabang_delivery_detail";

    /**
     * 商品表
     */
    String ORIGINAL_MABANG_COMBO_SKU = "original_mabang_combo_sku";

    /**
     * 管易云数据库表名
     */
    /**
     * 订单表
     */
    String ORIGINAL_GYY_ORDER = "original_gyy_order";

    /**
     * 商品表
     */
    String ORIGINAL_GYY_SKU = "original_gyy_sku";

    /**
     * 退货订单表
     */
    String ORIGINAL_GYY_RETURN_ORDER = "original_gyy_return_order";

    /**
     * 退款数据
     */
    String ORIGINAL_GYY_REFUND = "original_gyy_refund";

    /**
     * 店铺数据
     */
    String ORIGINAL_GYY_SHOP = "original_gyy_shop";


    /**
     * 出库详情
     */
    String ORIGINAL_GYY_DELIVERY_DETAIL = "original_gyy_delivery_detail";

    /**
     * 金蝶云星空数据库表名
     */
    /**
     * 订单表
     */
    String ORIGINAL_KINGDEE_ORDER = "original_kingdee_order";

    /**
     * 商品表
     */
    String ORIGINAL_KINGDEE_SKU = "original_kingdee_sku";

    /**
     * 退货订单表
     */
    String ORIGINAL_KINGDEE_RETURN_ORDER = "original_kingdee_return_order";

    /**
     * 退款数据
     */
    String ORIGINAL_KINGDEE_REFUND = "original_kingdee_refund";

    /**
     * 店铺数据
     */
    String ORIGINAL_KINGDEE_SHOP = "original_kingdee_shop";

    /**
     * 网店管理数据
     */
    String ORIGINAL_KINGDEE_ECC_SHOP = "original_kingdee_ecc_shop";

    /**
     * 出库详情
     */
    String ORIGINAL_KINGDEE_DELIVERY_DETAIL = "original_kingdee_delivery_detail";

    /**
     * 谷仓入库单
     */
    String ORIGINAL_GC_INBOUND_ORDER = "original_gc_inbound_order";

    /**
     * 艾姆勒入库单
     */
    String ORIGINAL_IML_INBOUND_ORDER = "original_iml_inbound_order";


    /**
     * 根据任务 key 获取表名
     * @param key
     * @return
     */
    static List<String> getTableListByTask(String key){
        switch (key){
            case TaskConstant.MABANG_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_MABANG_ORDER, ORIGINAL_MABANG_SKU, ORIGINAL_MABANG_RETURN_ORDER, ORIGINAL_MABANG_REFUND, ORIGINAL_MABANG_SHOP, ORIGINAL_MABANG_DELIVERY_DETAIL,ORIGINAL_MABANG_COMBO_SKU);
            case TaskConstant.GYY_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_GYY_ORDER, ORIGINAL_GYY_SKU, ORIGINAL_GYY_RETURN_ORDER, ORIGINAL_GYY_REFUND, ORIGINAL_GYY_SHOP, ORIGINAL_GYY_DELIVERY_DETAIL);
            case TaskConstant.KINGDEE_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_KINGDEE_ORDER, ORIGINAL_KINGDEE_SKU, ORIGINAL_KINGDEE_RETURN_ORDER, ORIGINAL_KINGDEE_REFUND, ORIGINAL_KINGDEE_SHOP, ORIGINAL_KINGDEE_ECC_SHOP, ORIGINAL_KINGDEE_DELIVERY_DETAIL);
            case TaskConstant.IML_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_IML_INBOUND_ORDER);
            default:
                return Collections.EMPTY_LIST;
        }
    }
}