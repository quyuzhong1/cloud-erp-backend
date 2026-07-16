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
 * AIYA（爱亚）库存概要查询请求参数。
 * <p>
 * 字段已按爱亚开放平台接口文档页面截图核对（2026-07-16），与《爱亚海外仓对接方案文档》6.2.4
 * 「库存数据」翻译稿存在两点出入，已按截图（更权威）为准：
 * <ul>
 *     <li>截图请求参数里<b>没有</b> {@code status} 字段（翻译稿里有，疑似跟 SKU 查询接口的
 *         {@code status} 混淆），本 DTO 已去掉该字段；</li>
 *     <li>截图多出一个翻译稿没提到的 {@code domainCode} 字段（可选，接口文档未给出参数描述，
 *         用途未知），本 DTO 已补上，详见 docs/integrations/aiya-overseas-warehouse/README.md
 *         「待产品确认」。</li>
 * </ul>
 * 截图确认的完整请求字段：{@code customerCode}（必填）/ {@code ignoreZero}（可选，默认FALSE）/
 * {@code domainCode}（可选，未知用途）/ {@code skus}（可选，单次不超过200个）/
 * {@code pageSize}（可选，{@code skus} 不存在时必填，默认200）/
 * {@code page}（可选，{@code skus} 不存在时必填）/ {@code stockStatus}（必填，但接口文档未给出可选
 * 枚举值——占位处理，详见 README「待产品确认」）/ {@code warehouseCode}（必填）。
 * <p>
 * 注意：接口字段名为 {@code page}，与 WEGO 的 {@code pageNum} 不同；本 DTO 沿用
 * {@code pageNum} 作为 Java 侧字段名（与项目内分页命名习惯一致，且与 {@link AiyaSkuQueryDTO} 保持一致），
 * 由 SDK 序列化时转换为 {@code page} key。
 */
@Data
@NoArgsConstructor
public class AiyaInventoryQueryDTO implements Serializable {

    /**
     * AIYA 库存查询单页默认条数（文档：pageSize 默认200）。
     */
    public static final int DEFAULT_PAGE_SIZE = 200;

    /**
     * 文档限制：{@code skus} 单次查询不能超过200个。
     */
    public static final int MAX_SKUS_PER_QUERY = 200;

    /**
     * 查询库存请求
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
         * 仓库code（文档：必填）
         */
        @NotBlank(message = "warehouseCode不能为空")
        private String warehouseCode;

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
         * 是否查询0库存sku（可选，文档默认FALSE，即正常返回0库存）
         */
        private Boolean ignoreZero;

        /**
         * 接口文档截图里出现但未给出参数描述的可选字段，用途未知（2026-07-16 核对接口文档截图时发现），
         * 详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」，默认不传。
         */
        private String domainCode;

        /**
         * sku编码集合（可选，文档限制单次查询不超过 {@link #MAX_SKUS_PER_QUERY} 个）
         */
        private List<String> skus;

        /**
         * 商品库存状态（必填，但接口文档未给出可选枚举值，取值需联调/产品确认，
         * 详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」）
         */
        private String stockStatus;

        /**
         * 附加业务参数，参与签名并一起发送（预留，便于文档未列出的字段透传）
         */
        private Map<String, Object> bizParams;
    }
}
