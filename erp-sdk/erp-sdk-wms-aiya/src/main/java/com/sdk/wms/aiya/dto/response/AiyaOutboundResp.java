package com.sdk.wms.aiya.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AIYA 2C 出库单查询响应结构（{@code GLINK_QUERY_ORDER_NOTIFY}）。
 * <p>
 * 顶层结构（2026-07-22 联调确认）：
 * {@code {success, code, message, total, orderInfoList:[]}}。
 * 明细状态字段以爱亚开放平台文档为准：{@code status}，枚举 {@code VALID}/{@code HELD}/{@code CANCELLED}
 * （方案文档字母码 A/B/C/D 为业务处理说明，不是网关真实字段名/取值）。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiyaOutboundResp implements Serializable {

    @JSONField(name = "success")
    private Boolean success;

    /**
     * 状态码（成功时通常为 SUCCESS；失败样例见过数字字符串如 {@code "139"}）
     */
    @JSONField(name = "code")
    private String code;

    @JSONField(name = "message")
    private String message;

    /**
     * 总记录数；文档：当执行成功时有值（失败样例可为 null）
     */
    @JSONField(name = "total")
    private Integer total;

    /**
     * 2C 出库单列表（2026-07-22 联调确认字段名）
     */
    @JSONField(name = "orderInfoList")
    private List<OutboundOrderDTO> orderInfoList;

    /**
     * 2C 出库单明细。
     * <p>
     * {@code orderNumber} 即建单幂等键（AIYA 不回传独立出库单号）。
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OutboundOrderDTO implements Serializable {

        /**
         * 订单编号（三方仓订单号 / 建单幂等键）
         */
        @JSONField(name = "orderNumber")
        private String orderNumber;

        /**
         * 仓库编码（文档响应映射未单独列出，透传保留）
         */
        @JSONField(name = "warehouseCode")
        private String warehouseCode;

        /**
         * 订单状态（官方字段名 {@code status}）：{@code VALID}/{@code HELD}/{@code CANCELLED}。
         * <p>
         * 兼容历史误用字段名 {@code orderStatus}。拦截收敛看 {@code CANCELLED}。
         * <p>
         * 注意：{@code VALID} 只代表订单未被拦截，<b>不等价于已发货</b>，是否已发货须结合 {@link #stage}
         * 判断，见 {@link com.sdk.wms.aiya.enums.AiyaEnums.StageEnum#isShipped(String)}。
         */
        @JSONField(name = "status", alternateNames = {"orderStatus"})
        private String status;

        /**
         * 订单阶段（官方字段名 {@code stage}，2026-07-29 联调真实报文确认存在，此前未接入）。
         * <p>
         * 枚举取值：{@code DUE_OUT}/{@code ALLOCATED}/{@code PICKING}/{@code PICKED}/{@code PACKING}/
         * {@code PACKED}/{@code SHIPPING}/{@code SHIPPED}/{@code CLOSED}/{@code PICK}/{@code PACK}/
         * {@code PARTIALLY_ALLOCATED}/{@code OPEN}/{@code CREATED}/{@code ROUTING}；
         * 仅 {@code SHIPPED} 经实测确认代表已发货，其余语义未逐一核实。
         */
        @JSONField(name = "stage")
        private String stage;

        /**
         * 订单创建时间（文档字段 {@code createTime}，类型标注为 {@code Date} 但网关按字符串下发，
         * 格式对齐其余时间字段 {@code yyyy-MM-dd HH:mm:ss}）。
         * <p>
         * 同一 {@code orderNumber} 出现多条重复记录时（正常流程不应出现），用于判定"最新"，
         * 优先于 {@link #orderCreatedTime}，见 {@code AiyaOutboundInitHandler#isNewer}。
         */
        @JSONField(name = "createTime")
        private String createTime;

        /**
         * 订单创建时间（文档字段 {@code orderCreatedTime}）；{@link #createTime} 缺失/解析失败时
         * 的去重兜底依据。
         */
        @JSONField(name = "orderCreatedTime")
        private String orderCreatedTime;

        /**
         * 发运时间（文档字段 {@code shippingTime}，格式 {@code yyyy-MM-dd HH:mm:ss}），
         * 对应 ERP {@code dateShipping}。
         */
        @JSONField(name = "shippingTime")
        private String shippingTime;

        /**
         * 实际物流 / 物流渠道（文档字段 {@code actualLogistic}；官方亦可能回 {@code carrier}）
         */
        @JSONField(name = "actualLogistic", alternateNames = {"carrier"})
        private String actualLogistic;

        /**
         * 承运商服务（官方可选字段 {@code carrierService}）
         */
        @JSONField(name = "carrierService")
        private String carrierService;

        /**
         * 运单号 / 物流跟踪号
         */
        @JSONField(name = "trackingNumber")
        private String trackingNumber;

        /**
         * 产品明细
         */
        @JSONField(name = "items")
        private List<ItemDTO> items;

        /**
         * 错误信息（提交失败/库存不足/出库异常时可能回填，文档未列，透传保留）
         */
        @JSONField(name = "errorMessage")
        private String errorMessage;
    }

    /**
     * 产品明细（文档：{@code sku}/{@code qty}）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemDTO implements Serializable {

        @JSONField(name = "sku")
        private String sku;

        /**
         * 数量（2026-07-29 联调真实报文确认官方字段名为 {@code quantity}，此前误用 {@code qty}
         * 导致该字段一直反序列化为空；保留 {@code qty} 作为兼容别名）
         */
        @JSONField(name = "quantity", alternateNames = {"qty"})
        private Integer qty;
    }
}
