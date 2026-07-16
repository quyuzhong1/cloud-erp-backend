package com.sdk.wms.aiya.constants;

/**
 * AIYA（爱亚）海外仓接口常量。
 * <p>
 * 骨架说明：本类参照 {@code WeGoConstants} 结构搭建，采用「单网关 + serviceType 路由」模式。
 * 爱亚网关地址即完整请求地址，无需额外 router 路径；各 {@code serviceType} 常量需按
 * AIYA 官方开放平台文档确认后替换（TODO）。
 */
public class AiyaConstants {

    private AiyaConstants() {
        throw new IllegalStateException("Utility AiyaConstants class");
    }

    /**
     * AIYA 接口域名-测试环境
     */
    public static final String BASE_URL = "http://edi-glink.800best.com/gateway/api/glink";

    /**
     * AIYA 接口域名-正式环境
     */
    public static final String BASE_URL_PROD = "http://edi-glink.800best.com/gateway/api/glink";

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
    public static final String GLINK_CREATE_ASN_NOTIFY = "GLINK_CREATE_ASN_NOTIFY";

    /**
     * 入库单验货明细查询（按上架完成时间范围分页查询 SKU 级验货明细，含良品/不良品）
     */
    public static final String GLINK_QUERY_ASN_INSPECT_DETAIL_NOTIFY = "GLINK_QUERY_ASN_INSPECT_DETAIL_NOTIFY";

    /**
     * 入库订单取消
     */
    public static final String GLINK_CANCEL_ASN_NOTIFY = "GLINK_CANCEL_ASN_NOTIFY";

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
