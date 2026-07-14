package com.sdk.wms.aiya.constants;

/**
 * AIYA（爱亚）海外仓接口常量。
 * <p>
 * 骨架说明：本类参照 {@code WeGoConstants} 结构搭建，采用「单网关 Router + interfaceType 路由」模式。
 * 以下 {@code BASE_URL} / {@code BASE_URL_PROD} / {@code ROUTER_PATH} 及各 {@code interfaceType}
 * 常量目前均为占位值，需按 AIYA 官方开放平台文档确认后替换（TODO）。
 */
public class AiyaConstants {

    private AiyaConstants() {
        throw new IllegalStateException("Utility AiyaConstants class");
    }

    /**
     * AIYA 接口域名-测试环境
     */
    public static final String BASE_URL = "http://kytest.800best.com/gateway/api/glink";

    /**
     * AIYA 接口域名-正式环境
     * <p>TODO：占位值，需替换为 AIYA 官方生产网关地址。
     */
    public static final String BASE_URL_PROD = "https://oms.aiya.example.com/api";

    /**
     * 统一 Router 路径
     * <p>TODO：占位值，需按 AIYA 文档确认。
     */
    public static final String ROUTER_PATH = "/open-api/router";

    /**
     * 查询仓库
     */
    public static final String GLINK_QUERY_WAREHOUSE_NOTIFY = "GLINK_QUERY_WAREHOUSE_NOTIFY";

    /**
     * 查询派送渠道
     */
    public static final String GLINK_QUERY_CARRIER_NOTIFY = "GLINK_QUERY_CARRIER_NOTIFY";

    /**
     * 产品查询
     */
    public static final String GLINK_QUERY_ITEM_NOTIFY = "GLINK_QUERY_ITEM_NOTIFY";

    /**
     * 入库订单创建/修改
     */
    public static final String INORDER_SAVE = "GLINK_CREATE_ASN_NOTIFY";

    /**
     * 入库订单分页查询
     */
    public static final String INORDER_QUERY_PAGE = "inorder.queryPage";

    /**
     * 入库订单取消
     */
    public static final String INORDER_CANCEL = "inorder.cancel";

    /**
     * 2C订单创建/修改
     */
    public static final String TWO_C_ORDER_SAVE = "2c.order.save";

    /**
     * 2C订单查询
     */
    public static final String TWO_C_ORDER_SEARCH = "2c.order.search";

    /**
     * 2C订单分页查询
     */
    public static final String TWO_C_ORDER_QUERY_PAGE = "2c.order.queryPage";

    /**
     * 2C订单截单/取消
     */
    public static final String TWO_C_ORDER_INTERCEPT = "2c.order.intercept";

    /**
     * 2C库存查询
     */
    public static final String TWO_C_INVENTORY_SEARCH = "2c.inventory.search";

    /**
     * 退货订单分页查询
     */
    public static final String RETURN_ORDER_QUERY_PAGE = "returnorder.queryPage";

}
