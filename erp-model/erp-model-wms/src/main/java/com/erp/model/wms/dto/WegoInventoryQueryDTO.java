package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * WEGO 2C 库存查询（2c.inventory.search）开放接口请求参数。
 * <p>
 * 公共参数 {@code accessToken / interfaceType / sign} 在 SDK 内统一拼装，
 * 分页参数 {@code pageNum / pageSize} 作为一等字段，
 * 其余可选过滤参数通过 {@link QueryReqDTO#bizParams} 透传。
 */
@Data
@NoArgsConstructor
public class WegoInventoryQueryDTO implements Serializable {

    /**
     * WEGO 2c.inventory.search 业务参数 key：仓库代码
     */
    public static final String BIZ_KEY_WAREHOUSE_CODE = "warehouseCode";

    /**
     * WEGO 2c.inventory.search 业务参数 key：SKU 编码（精确过滤单个 SKU）
     */
    public static final String BIZ_KEY_SKU = "sku";

    /**
     * 查询库存请求
     */
    @Data
    @NoArgsConstructor
    public static class QueryReqDTO {

        /**
         * WEGO accessToken
         */
        @NotBlank(message = "accessToken不能为空")
        private String accessToken;

        /**
         * WEGO secret（用于本地签名，不会发给第三方）
         */
        @NotBlank(message = "secret不能为空")
        private String secret;

        /**
         * 仓库代码，可选；不传则查询该授权下所有仓库库存
         */
        private String warehouseCode;

        /**
         * 页码，从 1 开始
         */
        @NotNull(message = "pageNum不能为空")
        @Min(value = 1, message = "pageNum最小为1")
        private Integer pageNum;

        /**
         * 页大小
         */
        @NotNull(message = "pageSize不能为空")
        @Min(value = 1, message = "pageSize最小为1")
        private Integer pageSize;

        /**
         * 附加业务参数，参与签名并一起发送。可包含：
         * <ul>
         *     <li>{@link #BIZ_KEY_WAREHOUSE_CODE}：仓库代码过滤，可选（与字段 warehouseCode 二选一，字段优先）</li>
         *     <li>{@link #BIZ_KEY_SKU}：按 SKU 精确过滤，可选</li>
         * </ul>
         */
        private Map<String, Object> bizParams;
    }
}
