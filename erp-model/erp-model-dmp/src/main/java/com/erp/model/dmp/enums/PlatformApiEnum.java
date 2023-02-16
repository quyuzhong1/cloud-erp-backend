package com.erp.model.dmp.enums;

public enum PlatformApiEnum {
    /**
     * 马帮api
     */
    //查询店铺列表
    SYS_GET_SHOP_LIST(1,"sys-get-shop-list"),
    //查询商品
    STOCK_DO_SEARCH_SKU_LIST(2,"stock-do-search-sku-list"),
    //订单退款列表
    ORDER_GET_REFUND_LIST(3,"order-get-refund-list"),
    //获取订单列表
//    ORDER_GET_ORDER_LIST(4,"order-get-order-list"),
    ORDER_GET_ORDER_LIST(4,"order-get-order-list-new"),

    /**
     * 获取历史订单列表
     */
    GET_HISTORY_ORDER_LIST(24,"get-history-order-list"),

    //获取退货订单数据
    ORDER_GET_RETURN_ORDER_LIST(5,"order-get-return-order-list"),
    //商品出库详情
    ORDER_GET_DELIVERY_LIST(16,"order-get-delivery-list"),

    /**
     * 管易云api
     */
    //获取订单列表
    GY_ERP_TRADE_GET(6,"gy.erp.trade.get"),
    //查询商品
    GY_ERP_ITEMS_GET(7,"gy.erp.items.get"),
    //获取退货订单数据
    GY_ERP_TRADE_RETURN_GET(8,"gy.erp.trade.return.get"),
    //订单退款列表
    GY_ERP_TRADE_REFUND_GET(9,"gy.erp.trade.refund.get"),
    //查询店铺列表
    GY_ERP_SHOP_GET(10,"gy.erp.shop.get"),
    //销售出库详情
    GY_ERP_TRADE_DELIVERY_GET(15,"gy.erp.trade.deliverys.get"),

    GY_ERP_TRADE_HISTORY_GET(19,"gy.erp.trade.history.get"),

    GY_ERP_TRADE_DELIVERYS_HISTORY_GET(20,"gy.erp.trade.deliverys.history.get"),


    /**
     * 金蝶云星空API
     */
    //获取订单列表
    SAL_SALEORDER(11,"SAL_SaleOrder"),
    //查询商品
    BD_MATERIAL(12,"BD_MATERIAL"),
    //获取退货订单数据
    SAL_RETURNSTOCK(13,"SAL_RETURNSTOCK"),
    //获取退款数据
    AR_REFUNDBILL(14,"AR_REFUNDBILL"),
    //销售出库详情
    SAL_OUTSTOCK(17,"SAL_OUTSTOCK"),

    //查询客户
    BD_CUSTOMER(21, "BD_Customer"),

    //网店管理
    ECC_SHOP(22, "ECC_Shop"),

    ;

    /** 状态码 */
    private Integer id;

    /** 任务名 */
    private String taskName;

    PlatformApiEnum(Integer id, String taskName) {
        this.id = id;
        this.taskName = taskName;
    }

    public Integer getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
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
}




