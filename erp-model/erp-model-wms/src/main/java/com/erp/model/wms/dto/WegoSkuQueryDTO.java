package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * WEGO SKU 查询请求参数
 */
@Data
@NoArgsConstructor
public class WegoSkuQueryDTO implements Serializable {

    /**
     * 查询 SKU 请求
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
         * 页大小
         */
        @NotNull(message = "pageSize不能为空")
        @Min(value = 1, message = "pageSize最小为1")
        private Integer pageSize;

        /**
         * 页码
         */
        @NotNull(message = "pageNum不能为空")
        @Min(value = 1, message = "pageNum最小为1")
        private Integer pageNum;

        /**
         * 附加业务参数，参与签名并一起发送
         */
        private Map<String, Object> bizParams;
    }
}
