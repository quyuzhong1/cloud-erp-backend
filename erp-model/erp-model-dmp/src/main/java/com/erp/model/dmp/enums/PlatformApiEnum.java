package com.erp.model.dmp.enums;

import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.TaskConstant;

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

    IML_OMS_ASN_LIST(23,"getAsnList", MongoTableNameContant.ORIGINAL_IML_INBOUND_ORDER, TaskConstant.IML_PULL_DATA_TASK),

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




