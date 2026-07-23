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
     * 入库单批量查询（按仓库 + 时间范围分页查询入库单信息，page/pageSize 分页，pageSize 最大 200）
     */
    public static final String GLINK_BATCH_QUERY_ASN_NOTIFY = "GLINK_BATCH_QUERY_ASN_NOTIFY";

    /**
     * 入库订单取消
     */
    public static final String GLINK_CANCEL_ASN_NOTIFY = "GLINK_CANCEL_ASN_NOTIFY";

    /**
     * 2C订单创建/修改
     */
    public static final String TWO_C_ORDER_SAVE = "GLINK_CREATE_ORDER_NOTIFY";

    /**
     * 2C订单查询（支持按单号列表/订单时间范围过滤，支持分页；{@code AiyaOpenApiService#query2cOrder}
     * 统一走该 serviceType，骨架时期曾拆成 search/queryPage 两个方法，因指向同一 serviceType 已合并）
     */
    public static final String TWO_C_ORDER_SEARCH = "GLINK_BATCH_QUERY_ORDER_NOTIFY";

    /**
     * 2C订单截单/取消
     */
    public static final String TWO_C_ORDER_INTERCEPT = "GLINK_CANCEL_ORDER_NOTIFY";

    /**
     * 2C库存查询
     */
    public static final String TWO_C_INVENTORY_SEARCH = "GLINK_QUERY_INVENTORY_NOTIFY_NEW";

    /**
     * 退货订单分页查询
     */
    public static final String RETURN_ORDER_QUERY_PAGE = "returnorder.queryPage";

}
