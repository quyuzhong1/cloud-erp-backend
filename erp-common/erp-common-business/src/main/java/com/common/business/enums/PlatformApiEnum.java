package com.common.business.enums;


import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.TaskConstant;

import java.util.Arrays;

public enum PlatformApiEnum {
    /**
     * 马帮api
     */
    //查询店铺列表
    SYS_GET_SHOP_LIST(1,"sys-get-shop-list", MongoTableNameContant.ORIGINAL_MABANG_SHOP, TaskConstant.MABANG_PULL_DATA_TASK),
    //查询商品
    STOCK_DO_SEARCH_SKU_LIST(2,"stock-do-search-sku-list-new", MongoTableNameContant.ORIGINAL_MABANG_SKU, TaskConstant.MABANG_PULL_DATA_TASK),
    //订单退款列表
    ORDER_GET_REFUND_LIST(3,"order-get-refund-list", MongoTableNameContant.ORIGINAL_MABANG_REFUND, TaskConstant.MABANG_PULL_DATA_TASK),
    //获取订单列表
//    ORDER_GET_ORDER_LIST(4,"order-get-order-list"),
    ORDER_GET_ORDER_LIST(4,"order-get-order-list-new", MongoTableNameContant.ORIGINAL_MABANG_ORDER, TaskConstant.MABANG_PULL_DATA_TASK),

    /**
     * 获取历史订单列表
     */
    GET_HISTORY_ORDER_LIST(24,"get-history-order-list", "", TaskConstant.MABANG_PULL_DATA_TASK),

    //获取退货订单数据
    ORDER_GET_RETURN_ORDER_LIST(5,"order-get-return-order-list", MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, TaskConstant.MABANG_PULL_DATA_TASK),
    /**
     * 获取出库单列表
     */
    ORDER_GET_DELIVERY_LIST(16,"order-get-delivery-list", MongoTableNameContant.ORIGINAL_MABANG_DELIVERY_DETAIL, TaskConstant.MABANG_PULL_DATA_TASK),

    /**
     * 获取历史商品出库详情
     */
    ORDER_GET_HISTORY_DELIVERY_LIST(24,"order-get-history-delivery-list", "", TaskConstant.MABANG_PULL_DATA_TASK),
    /**
     * 查询组合商品
     */
    STOCK_DO_SEARCH_COMBO_SKU(29,"stock-do-search-combo-sku", MongoTableNameContant.ORIGINAL_MABANG_COMBO_SKU, TaskConstant.MABANG_PULL_DATA_TASK),

    /**
     * 马帮手工出库
     */
    MABANG_OUT_STORAGE(31,"warehouse-do-add-storage-out", "", ""),

    /**
     * 马帮手工入库
     */
    MABANG_IN_STORAGE(32,"warehouse-do-add-storage-in", "", ""),

    /**
     * 马帮调拨发货列表
     */
    MABANG_SHIPMENT(33,"hwc-shippbatch-get-shipment-list", MongoTableNameContant.ORIGINAL_MABANG_SHIPMENT, TaskConstant.MABANG_PULL_DATA_TASK_24),

    MABANG_DELIVERY(35,"hwc-get-batch-delivery-list", MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, TaskConstant.MABANG_PULL_DATA_TASK_24),


    /**
     * 管易云api
     */
    //获取订单列表
    GY_ERP_TRADE_GET(6,"gy.erp.trade.get", MongoTableNameContant.ORIGINAL_GYY_ORDER, TaskConstant.GYY_PULL_DATA_TASK),
    //查询商品
    GY_ERP_ITEMS_GET(7,"gy.erp.items.get", MongoTableNameContant.ORIGINAL_GYY_SKU, TaskConstant.GYY_PULL_DATA_TASK),
    //获取退货订单数据
    GY_ERP_TRADE_RETURN_GET(8,"gy.erp.trade.return.get", MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, TaskConstant.GYY_PULL_DATA_TASK),
    //订单退款列表
    GY_ERP_TRADE_REFUND_GET(9,"gy.erp.trade.refund.get", MongoTableNameContant.ORIGINAL_GYY_REFUND, TaskConstant.GYY_PULL_DATA_TASK),
    //查询店铺列表
    GY_ERP_SHOP_GET(10,"gy.erp.shop.get", MongoTableNameContant.ORIGINAL_GYY_SHOP, TaskConstant.GYY_PULL_DATA_TASK),
    //销售出库详情
    GY_ERP_TRADE_DELIVERY_GET(15,"gy.erp.trade.deliverys.get", MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL, TaskConstant.GYY_PULL_DATA_TASK),

    GY_ERP_TRADE_HISTORY_GET(19,"gy.erp.trade.history.get", "", TaskConstant.GYY_PULL_DATA_TASK),

    GY_ERP_TRADE_DELIVERYS_HISTORY_GET(20,"gy.erp.trade.deliverys.history.get", "", TaskConstant.GYY_PULL_DATA_TASK),

    GY_ERP_TRADE_DELIVERYS_DETAIL_GET(25, "gy.erp.trade.deliverys.detail.get", "", TaskConstant.GYY_PULL_DATA_TASK),

    GY_ERP_TRADE_DELIVERYS_DETAIL_HISTORY_GET(26, "gy.erp.trade.deliverys.detail.history.get", "", TaskConstant.GYY_PULL_DATA_TASK),

    GY_ERP_TRADE_DETAIL_GET(27, "gy.erp.trade.detail.get", "", TaskConstant.GYY_PULL_DATA_TASK),

    GY_ERP_TRADE_HISTORY_DETAIL_GET(28, "gy.erp.trade.history.detail.get", "", TaskConstant.GYY_PULL_DATA_TASK),

    /**
     * 金蝶云星空API
     */
    //获取订单列表
    SAL_SALEORDER(11,"SAL_SaleOrder", MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, TaskConstant.KINGDEE_PULL_DATA_TASK),
    //查询商品
    BD_MATERIAL(12,"BD_MATERIAL", MongoTableNameContant.ORIGINAL_KINGDEE_SKU, TaskConstant.KINGDEE_PULL_DATA_TASK),
    //获取退货订单数据
    SAL_RETURNSTOCK(13,"SAL_RETURNSTOCK", MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, TaskConstant.KINGDEE_PULL_DATA_TASK),
    //获取退款数据
    AR_REFUNDBILL(14,"AR_REFUNDBILL", MongoTableNameContant.ORIGINAL_KINGDEE_REFUND, TaskConstant.KINGDEE_PULL_DATA_TASK),
    //销售出库详情
    SAL_OUTSTOCK(17,"SAL_OUTSTOCK", MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, TaskConstant.KINGDEE_PULL_DATA_TASK),

    //查询客户
    BD_CUSTOMER(21, "BD_Customer", MongoTableNameContant.ORIGINAL_KINGDEE_SHOP, TaskConstant.KINGDEE_PULL_DATA_TASK),

    //网店管理
    ECC_SHOP(22, "ECC_Shop", MongoTableNameContant.ORIGINAL_KINGDEE_ECC_SHOP, TaskConstant.KINGDEE_PULL_DATA_TASK),

    STK_TRANSFERDIRECT(23,"STK_TransferDirect", MongoTableNameContant.ORIGINAL_KINGDEE_DIRECT_TRANSFER, TaskConstant.KINGDEE_PULL_DATA_TASK),

    IML_OMS_ASN_LIST(24,"getAsnList", MongoTableNameContant.ORIGINAL_IML_INBOUND_ORDER, TaskConstant.IML_PULL_DATA_TASK),

    BD_RATE(25,"BD_Rate", MongoTableNameContant.ORIGINAL_KINGDEE_EXCHANGE_RATE, TaskConstant.KINGDEE_PULL_DATA_TASK),

    GOODCANG_PRODUCT(26,"GOODCANG_PRODUCT", MongoTableNameContant.ORIGINAL_GC_PRODUCT, TaskConstant.GOODCANG_PULL_DATA_TASK),

    TRACK123_GET_TRACK(27,"getTask", MongoTableNameContant.ORIGINAL_GC_PRODUCT, TaskConstant.GOODCANG_PULL_DATA_TASK),

    /**
     * 领星api
     */
    // 店铺信息
    LX_ERP_SHOP_LIST_GET(60, "getShopList", MongoTableNameContant.ORIGINAL_LX_SHOP_LIST, TaskConstant.LX_PULL_DATA_TASK),
    LX_ERP_FBA_SHIPMENT_RECEIVE_GET(61, "getFbaReceive", MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, TaskConstant.LX_PULL_DATA_TASK),

    /**
     * 亚马逊api
     */
    FBA_SHIPMENT_DETAIL(65, "fba_shipment_detail", MongoTableNameContant.THIRD_SYSTEM_AMAZON_FBA_SHIPMENT, ""),
    AMZ_LISTING_REPORT(66, "listingReport", MongoTableNameContant.THIRD_SYSTEM_AMAZON_LISTING, ""),


    //----------------------------------------------------------旺店通----------------------------------------------------------------------------
    WDT_SELL_STOCK_OUT_ORDER(100, "wdt.wms.stockout.Sales.queryWithDetail", MongoTableNameContant.THIRD_SYSTEM_WDT_SELL_STOCK_OUT_ORDER, TaskConstant.WDT_PULL_DATA_TASK),
    WDT_RETURN_STOCK_OUT_ORDER(101, "wdt.wms.stockin.Refund.queryWithDetail", MongoTableNameContant.THIRD_SYSTEM_WDT_RETURN_STOCK_OUT_ORDER, TaskConstant.WDT_PULL_DATA_TASK),
    WANGDIAN_SHOP(102, "setting.Shop.queryShop", MongoTableNameContant.ORIGNAL_WANGDIAN_SHOP, TaskConstant.WDT_PULL_DATA_TASK),
    WANGDIAN_WAREHOUSE(103, "setting.Warehouse.queryWarehouse", MongoTableNameContant.ORIGNAL_WANGDIAN_WAREHOUSE, TaskConstant.WDT_PULL_DATA_TASK),
    WANGDIAN_VIRTUAL_WAREHOUSE(104, "setting.strategy.VirtualWarehouse.warehouseSearch", MongoTableNameContant.ORIGNAL_WANGDIAN_VIRTUAL_WAREHOUSE, TaskConstant.WDT_PULL_DATA_TASK),
    ;

    /** 状态码 */
    private Integer id;

    /** 任务名 */
    private String taskName;

    private String mongoTableName;

    private String platformTaskName;

    PlatformApiEnum(Integer id, String taskName, String mongoTableName, String platformTaskName) {
        this.id = id;
        this.taskName = taskName;
        this.mongoTableName = mongoTableName;
        this.platformTaskName = platformTaskName;
    }

    public Integer getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getMongoTableName() {
        return mongoTableName;
    }

    public String getPlatformTaskName() {
        return platformTaskName;
    }

    public static String getNameById(Integer id) {
        PlatformApiEnum[] platformApiEnums = values();
        for (PlatformApiEnum platformApiEnum : platformApiEnums) {
            if (platformApiEnum.getId().equals(id)) {
                return platformApiEnum.getTaskName();
            }
        }
        return null;
    }

    public static PlatformApiEnum getEnumByType(String taskName){
        PlatformApiEnum[] platformApiEnums = values();
        for (PlatformApiEnum platformApiEnum : platformApiEnums) {
            if (platformApiEnum.getTaskName().equals(taskName)) {
                return platformApiEnum;
            }
        }
        return null;
    }

    public static Integer getIdByTaskName(String taskName) {
        PlatformApiEnum[] platformApiEnums = values();
        for (PlatformApiEnum platformApiEnum : platformApiEnums) {
            if (platformApiEnum.getTaskName().equals(taskName)) {
                return platformApiEnum.getId();
            }
        }
        return null;
    }
    public static PlatformApiEnum getByMongoTable(String mongoTableName){
        return Arrays.stream(values()).filter(platformApiEnum -> platformApiEnum.getMongoTableName().equals(mongoTableName))
                .findFirst()
                .orElse(null);
    }
}




