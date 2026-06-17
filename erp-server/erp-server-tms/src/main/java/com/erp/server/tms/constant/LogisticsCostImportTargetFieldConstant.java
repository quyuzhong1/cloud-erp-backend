package com.erp.server.tms.constant;

/**
 * 物流费用导入 / 对账匹配模板 targetField 常量（对齐 LogisticsBillVo 及导入配置字段名）。
 *
 * @author Will
 * @date 2026/6/12
 */
public final class LogisticsCostImportTargetFieldConstant {

    private LogisticsCostImportTargetFieldConstant() {
    }

    /**
     * 销售单号（sourceCode）
     */
    public static final String SOURCE_CODE = "sourceCode";

    /**
     * 平台订单号（platformCode）
     */
    public static final String PLATFORM_CODE = "platformCode";

    /**
     * 物流跟踪号（trackNo）
     */
    public static final String TRACK_NO = "trackNo";

    /**
     * 物流运单号（transportNo）
     */
    public static final String TRANSPORT_NO = "transportNo";

    /**
     * 发货单号（soDeliveryCode）
     */
    public static final String SO_DELIVERY_CODE = "soDeliveryCode";

    /**
     * 销售单号别名（部分模板 targetField 使用 soCode）
     */
    public static final String SO_CODE = "soCode";

    /**
     * 平台订单号别名（部分模板 targetField 使用 platformOrderNo）
     */
    public static final String PLATFORM_ORDER_NO = "platformOrderNo";
}
