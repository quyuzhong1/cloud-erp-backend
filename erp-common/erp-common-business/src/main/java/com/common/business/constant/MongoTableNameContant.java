package com.common.business.constant;

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
     * 直接调拨单
     */
    String ORIGINAL_KINGDEE_DIRECT_TRANSFER = "original_kingdee_direct_transfer";

    /**
     * 汇率
     */
    String ORIGINAL_KINGDEE_EXCHANGE_RATE = "original_kingdee_exchange_rate";

    /**
     * 谷仓入库单
     */
    String ORIGINAL_GC_INBOUND_ORDER = "original_gc_inbound_order";

    /**
     * 谷仓产品数据
     */
    String ORIGINAL_GC_PRODUCT = "original_gc_product";

    /**
     * 艾姆勒入库单
     */
    String ORIGINAL_IML_INBOUND_ORDER = "original_iml_inbound_order";

    /**
     * 马帮调拨发货列表
     */
    String ORIGINAL_MABANG_SHIPMENT = "original_mabang_shipment";

    /**
     * 马帮发货单列表
     */
    String ORIGINAL_MABANG_DELIVERY = "original_mabang_fba_delivery";

    // 亚马逊数据库表名
    /**
     * 亚马逊订单表
     */
    String THIRD_SYSTEM_AMAZON_ORDER = "third_system_Amazon_order";

    /**
     * 亚马逊商品表
     */
    String THIRD_SYSTEM_AMAZON_PRODUCT = "third_system_Amazon_product";

    /**
     * 亚马逊销售出库表
     */
    String THIRD_SYSTEM_AMAZON_SO_OUT_STOCK = "third_system_Amazon_so_out_stock";

    /**
     * 亚马逊报表表
     */
    String THIRD_SYSTEM_AMAZON_REPORT = "third_system_Amazon_report";

    /**
     * 亚马逊FBA货件表
     */
    String THIRD_SYSTEM_AMAZON_FBA_SHIPMENT = "third_system_Amazon_fba_shipment";

    /**
     * 亚马逊FBA货件表
     */
    String THIRD_SYSTEM_AMAZON_LISTING = "third_system_Amazon_listing";

    /**
     * 亚马逊物流管理库存状况报告
     */
    String DATA_REPORT_AMZ_FBA_INVENTORY_PLANNING = "data_report_amz_fba_inventory_planning";

    /**
     * 亚马逊商品报告表
     */
    String DATA_REPORT_AMZ_LISTING = "data_report_amz_listing";

    /**
     * 亚马逊物流管理库存-已存档
     */
    String DATA_REPORT_AMZ_FBA_MYI_ALL_INVENTORY = "data_report_amz_fba_myi_all_inventory";

    /**
     * 亚马逊物流管理库存-未存档
     */
    String DATA_REPORT_AMZ_FBA_MYI_UNSUPPRESSED_INVENTORY = "data_report_amz_fba_myi_unsuppressed_inventory";

    /**
     * 亚马逊物流预留库存报告
     */
    String DATA_REPORT_AMZ_RESERVED = "data_report_amz_reserved";

    /**
     * 亚马逊物流销售报告
     */
    String DATA_REPORT_AMZ_FULFILLED_SHIPMENTS = "data_report_amz_fulfilled_shipments";


    // Shopify数据库表名
    /**
     * Shopify订单表
     */
    String THIRD_SYSTEM_SHOPIFY_ORDER = "third_system_Shopify_order";
    /**
     * 虾皮订单表
     */
    String THIRD_SYSTEM_SHOPEE_ORDER = "third_system_Shopee_order";

    /**
     * 虾皮商品表
     */
    String THIRD_SYSTEM_SHOPEE_PRODUCT = "third_system_Shopee_product";


    /**
     * 速卖通订单表
     */
    String THIRD_SYSTEM_ALI_EXPRESS_ORDER = "third_system_AliExpress_order";

    /**
     * 领星店铺信息表
     */
    String ORIGINAL_LX_SHOP_LIST = "original_lx_shop_list";

    /**
     * 领星签收明细表
     */
    String ORIGINAL_LX_FBA_SHIPMENT_RECEIVE = "original_lx_fba_shipment_receive";

    /**
     * 旺店通销售出库单
     */
    String THIRD_SYSTEM_WDT_SELL_STOCK_OUT_ORDER = "third_system_wdt_sell_stock_out_order";

    /**
     * 旺店通销售出库单
     */
    String THIRD_SYSTEM_WDT_RETURN_STOCK_OUT_ORDER = "third_system_wdt_return_stock_out_order";
    String ORIGNAL_WANGDIAN_WAREHOUSE = "orignal_wangdian_warehouse";
    String ORIGNAL_WANGDIAN_SHOP = "orignal_wangdian_shop";
    String ORIGNAL_WANGDIAN_VIRTUAL_WAREHOUSE = "orignal_wangdian_virtual_warehouse";
    /**
     * 根据任务 key 获取表名
     *
     * @param key
     * @return
     */
    static List<String> getTableListByTask(String key) {
        switch (key) {
            case TaskConstant.MABANG_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_MABANG_ORDER, ORIGINAL_MABANG_SKU, ORIGINAL_MABANG_RETURN_ORDER, ORIGINAL_MABANG_REFUND, ORIGINAL_MABANG_SHOP, ORIGINAL_MABANG_DELIVERY_DETAIL, ORIGINAL_MABANG_COMBO_SKU);
            case TaskConstant.GYY_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_GYY_ORDER, ORIGINAL_GYY_SKU, ORIGINAL_GYY_RETURN_ORDER, ORIGINAL_GYY_REFUND, ORIGINAL_GYY_SHOP, ORIGINAL_GYY_DELIVERY_DETAIL);
            case TaskConstant.KINGDEE_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_KINGDEE_ORDER, ORIGINAL_KINGDEE_SKU, ORIGINAL_KINGDEE_RETURN_ORDER, ORIGINAL_KINGDEE_SHOP, ORIGINAL_KINGDEE_ECC_SHOP, ORIGINAL_KINGDEE_DELIVERY_DETAIL, ORIGINAL_KINGDEE_DIRECT_TRANSFER);
            case TaskConstant.IML_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_IML_INBOUND_ORDER);
            case TaskConstant.LX_PULL_DATA_TASK:
                return Arrays.asList(ORIGINAL_LX_SHOP_LIST, ORIGINAL_LX_FBA_SHIPMENT_RECEIVE);
            case TaskConstant.WDT_PULL_DATA_TASK:
                return Arrays.asList(THIRD_SYSTEM_WDT_SELL_STOCK_OUT_ORDER, THIRD_SYSTEM_WDT_RETURN_STOCK_OUT_ORDER,ORIGNAL_WANGDIAN_WAREHOUSE,ORIGNAL_WANGDIAN_SHOP,ORIGNAL_WANGDIAN_VIRTUAL_WAREHOUSE);
            default:
                return Collections.EMPTY_LIST;
        }
    }
}