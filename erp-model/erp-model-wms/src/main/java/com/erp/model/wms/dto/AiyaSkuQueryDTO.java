package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * AIYA（爱亚）SKU 查询请求参数。
 * <p>
 * 参照 {@link WegoSkuQueryDTO} 搭建，字段语义对齐《爱亚海外仓对接方案文档》商品注册/查询接口：
 * 请求字段为 {@code status}（可选，商品使用状态）/ {@code pageSize}（可选，默认200）/
 * {@code page}（可选，页码）/ {@code customerCode}（必填，由 SDK 统一注入 bizData，不在此 DTO 中）。
 * <p>
 * 注意：文档请求字段名为 {@code page}，与 WEGO 的 {@code pageNum} 不同；本 DTO 沿用
 * {@code pageNum} 作为 Java 侧字段名（与项目内分页命名习惯一致），由 SDK 序列化时转换为 {@code page} key。
 */
@Data
@NoArgsConstructor
public class AiyaSkuQueryDTO implements Serializable {

    /**
     * AIYA SKU 查询单页默认条数（文档：pageSize 默认200）。
     */
    public static final int DEFAULT_PAGE_SIZE = 200;

    /**
     * 查询 SKU 请求
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
         * AIYA 客户code（爱亚所有接口必填业务参数，由 SDK 统一注入 bizData）
         */
        @NotBlank(message = "customerCode不能为空")
        private String customerCode;

        /**
         * 页大小，文档默认200
         */
        @NotNull(message = "pageSize不能为空")
        @Min(value = 1, message = "pageSize最小为1")
        @Max(value = 1000, message = "pageSize最大为1000")
        private Integer pageSize;

        /**
         * 页码（Java侧命名为 pageNum，序列化时对应文档字段 page）
         */
        @NotNull(message = "pageNum不能为空")
        @Min(value = 1, message = "pageNum最小为1")
        private Integer pageNum;

        /**
         * 商品使用状态过滤（可选），取值参照 {@code com.sdk.wms.aiya.enums.AiyaSkuStatusEnum}
         */
        private String status;

        /**
         * 附加业务参数，参与签名并一起发送（预留，便于文档未列出的字段透传）
         */
        private Map<String, Object> bizParams;
    }
}
