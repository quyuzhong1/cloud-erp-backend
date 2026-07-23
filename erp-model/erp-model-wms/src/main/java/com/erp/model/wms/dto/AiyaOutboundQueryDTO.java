package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * AIYA（爱亚/百世 GLINK）2C 出库单查询（serviceType={@code GLINK_QUERY_ORDER_NOTIFY}）请求报文。
 * <p>
 * 字段按《爱亚海外仓对接方案文档》6.3.3「4、爱亚出库单查询」请求参数表落地：
 * <ul>
 *     <li>必填：{@code customerCode}（SDK 注入）、{@code warehouseCode}；</li>
 *     <li>可选：{@code shippingTimeFrom}/{@code shippingTimeTo}（发运时间窗口，DMP 定时拉取用）、
 *         {@code page}/{@code pageSize}。方案文档写的 {@code pageNum} 有误，真实网关字段为 {@code page}
 *         （与 SKU/库存/入库查询一致）；本 DTO Java 侧仍用 {@code pageNum}，由 SDK 序列化为 {@code page}。</li>
 * </ul>
 * 注意：勿与入库单查询的 {@code putawayCompletedTimeFrom}/{@code putawayCompletedTimeTo}（上架时间）混淆，
 * 也勿使用建单字段 {@code orderTime}——查询侧过滤键是发运时间 {@code shippingTime*}。
 * <p>
 * 响应结构见 {@code AiyaOutboundResp}（顶层 {@code orderInfoList}，2026-07-22 联调确认）。
 */
@Data
@NoArgsConstructor
public class AiyaOutboundQueryDTO implements Serializable {

    /**
     * 出库单查询单页默认条数。
     */
    public static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 查询 2C 出库单请求。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryReqDTO implements Serializable {

        /**
         * AIYA partnerId（客户ID）
         */
        @NotBlank(message = "accessToken不能为空")
        private String accessToken;

        /**
         * AIYA partnerKey（仅用于本地签名，不会发给第三方）
         */
        @NotBlank(message = "secret不能为空")
        private String secret;

        /**
         * 用户编码（文档必填），由 SDK 统一注入 bizData。
         */
        @NotBlank(message = "customerCode不能为空")
        private String customerCode;

        /**
         * 仓库编码（文档必填）。
         */
        @NotBlank(message = "warehouseCode不能为空")
        private String warehouseCode;

        /**
         * 发运时间开始（可选，格式 {@code yyyy-MM-dd HH:mm:ss}；方案文档业务约定按 [date-1]~[date] 窗口拉取）。
         */
        private String shippingTimeFrom;

        /**
         * 发运时间结束（可选）。
         */
        private String shippingTimeTo;

        /**
         * 页码（Java 侧命名 {@code pageNum}，SDK 序列化为真实网关字段 {@code page}；
         * 方案文档写 pageNum 有误）。
         */
        @NotNull(message = "pageNum不能为空")
        @Min(value = 1, message = "pageNum最小为1")
        private Integer pageNum;

        /**
         * 每页数量（文档字段 {@code pageSize}）。
         */
        @NotNull(message = "pageSize不能为空")
        @Min(value = 1, message = "pageSize最小为1")
        private Integer pageSize;

        /**
         * 附加业务参数（预留，便于文档未列出的字段透传）。
         */
        private Map<String, Object> bizParams;
    }
}
