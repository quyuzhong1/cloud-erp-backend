package com.sdk.wms.wego.constants;

public class WeGoConstants {

    private WeGoConstants() {
        throw new IllegalStateException("Utility WeGoConstants class");
    }

    /**
     * WEGO 接口域名-测试环境
     */
    public static final String BASE_URL = "http://wego.oms.utech.work/api";

    /**
     * WEGO 接口域名-正式环境
     */
    public static final String BASE_URL_PROD = "http://oms.wegocorp.net/api";

    public static final String ROUTER_PATH = "/open-api/router";

    /**
     * 查询仓库
     */
    public static final String WAREHOUSE_GET = "warehouse.get";

    /**
     * 查询派送渠道
     */
    public static final String TRANSPORT_GET = "transport.get";

    /**
     * 产品创建
     */
    public static final String PRODUCT_SAVE = "product.save";

    /**
     * 产品查询
     */
    public static final String PRODUCT_SEARCH = "product.search";

    /**
     * 入库订单创建/修改
     */
    public static final String INORDER_SAVE = "inorder.save";

    /**
     * 入库订单查询
     */
    public static final String INORDER_SEARCH = "inorder.search";

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
     * 2C订单取消
     */
    public static final String TWO_C_ORDER_INTERCEPT = "2c.order.intercept";

    /**
     * 2C订单加急
     */
    public static final String TWO_C_ORDER_URGENT = "2c.order.urgent";

    /**
     * 2C库存查询
     */
    public static final String TWO_C_INVENTORY_SEARCH = "2c.inventory.search";

    /**
     * 退货订单创建/修改
     */
    public static final String RETURN_ORDER_SAVE = "returnorder.save";

    /**
     * 退货订单取消
     */
    public static final String RETURN_ORDER_CANCEL = "returnorder.cancel";

    /**
     * 退货订单查询
     */
    public static final String RETURN_ORDER_SEARCH = "returnorder.search";

    /**
     * 退货订单分页查询
     */
    public static final String RETURN_ORDER_QUERY_PAGE = "returnorder.queryPage";

}
