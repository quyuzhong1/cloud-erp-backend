package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * AIYA（爱亚/百世 GLINK）入库单批量查询（serviceType={@code GLINK_BATCH_QUERY_ASN_NOTIFY}）请求报文。
 * <p>
 * 字段按爱亚开放平台《入库单批量查询》接口业务参数逐一承载：{@code customerCode}/{@code page}/
 * {@code pageSize}/{@code warehouseCode} 为文档必填；其余为可选字段（不传即不写入 bizData）。
 * <p>
 * 业务约定（{@code AiyaInboundInitHandler} 使用方）：
 * <ul>
 *     <li>本 DTO 固定以「上架完成时间」窗口拉取，因此 {@code putawayCompletedTimeFrom}/
 *         {@code putawayCompletedTimeTo} 在本项目视为必填（{@code @NotBlank}），须成对传入；</li>
 *     <li>分页字段 Java 侧命名为 {@code pageNum}，序列化时对应文档字段 {@code page}；</li>
 *     <li>{@code pageSize} 单页最大 200，需按总数多次翻页调用。</li>
 * </ul>
 * 响应字段较多，另行由 {@code AiyaInboundResp} 承载，本 DTO 仅负责请求侧。
 */
@Data
@NoArgsConstructor
public class AiyaInboundQueryDTO implements Serializable {

    /**
     * 入库单批量查询单页默认条数（文档：pageSize 最大 200）。
     */
    public static final int DEFAULT_PAGE_SIZE = 200;

    /**
     * 入库单批量查询单页最大条数。
     */
    public static final int MAX_PAGE_SIZE = 200;

    /**
     * 入库单批量查询请求
     */
    @Data
    @NoArgsConstructor
    public static class QueryReqDTO {

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
         * 页码（文档必填，Java 侧命名为 pageNum，序列化时对应文档字段 page）。
         */
        @NotNull(message = "pageNum不能为空")
        @Min(value = 1, message = "pageNum最小为1")
        private Integer pageNum;

        /**
         * 每页记录数（文档必填，最大 200）。
         */
        @NotNull(message = "pageSize不能为空")
        @Min(value = 1, message = "pageSize最小为1")
        @Max(value = MAX_PAGE_SIZE, message = "pageSize最大为200")
        private Integer pageSize;

        /**
         * 仓库编码（文档必填）。
         */
        @NotBlank(message = "warehouseCode不能为空")
        private String warehouseCode;

        /**
         * 上架完成时间从（可选，格式 {@code yyyy-MM-dd HH:mm:ss}）。
         * <p>
         * 注意：{@code putawayCompletedTime} 不在爱亚「至少一组非空」的必填时间集合内（见 {@link #receiveTimeFrom}），
         * 单独传会报 {@code INVALID_DATA}；此处仅作附加过滤，需与 createdTime/receiveTime/lastUpdatedTime 之一同时传。
         */
        private String putawayCompletedTimeFrom;

        /**
         * 上架完成时间到（可选，格式 {@code yyyy-MM-dd HH:mm:ss}）。
         */
        private String putawayCompletedTimeTo;

        /**
         * 入库单号列表（可选，单个 ≤ 64；若 receiveTime 范围不为空，此字段可为空）。
         */
        private List<String> asnNumbers;

        /**
         * 入库单类型（可选），枚举：{@code SUPPLIER_RECEIPT}、{@code RETURN}。
         */
        private String asnType;

        /**
         * 收货时间从（格式 {@code yyyy-MM-dd HH:mm:ss}）。
         * <p>
         * 爱亚隐藏约束：{@code createdTime}/{@code receiveTime}/{@code lastUpdatedTime}/{@code asnNumbers}/
         * {@code refNumbers} 至少一组非空，否则报 {@code INVALID_DATA: ... cannot be empty at the same time}。
         * 本项目增量拉取统一以「收货时间」窗口满足该约束（若 asnNumbers 不为空，此字段可为空）。
         */
        private String receiveTimeFrom;

        /**
         * 收货时间到（格式 {@code yyyy-MM-dd HH:mm:ss}），需与 {@link #receiveTimeFrom} 成对传入。
         */
        private String receiveTimeTo;

        /**
         * 创建时间从（可选，格式 {@code yyyy-MM-dd HH:mm:ss}）。
         */
        private String createdTimeFrom;

        /**
         * 创建时间到（可选，格式 {@code yyyy-MM-dd HH:mm:ss}）。
         */
        private String createdTimeTo;

        /**
         * 更新时间从（可选，格式 {@code yyyy-MM-dd HH:mm:ss}）。可满足爱亚「至少一组非空」约束，
         * 但本项目增量拉取统一使用 {@link #receiveTimeFrom}「收货时间」窗口，此字段仅作附加过滤。
         */
        private String lastUpdatedTimeFrom;

        /**
         * 更新时间到（格式 {@code yyyy-MM-dd HH:mm:ss}），需与 {@link #lastUpdatedTimeFrom} 成对传入。
         */
        private String lastUpdatedTimeTo;

        /**
         * 入库单阶段（可选）。
         */
        private String stage;

        /**
         * 是否查询入库单批次信息（可选）。
         */
        private Boolean ifNeedBatchInfo;

        /**
         * 参考单号列表（可选）。
         */
        private List<String> refNumbers;

        /**
         * 外部用户 ID（可选）。
         */
        private String extUserId;

        /**
         * 附加业务参数，参与签名并一起发送（预留，便于文档未列出的字段透传）。
         */
        private Map<String, Object> bizParams;
    }
}
