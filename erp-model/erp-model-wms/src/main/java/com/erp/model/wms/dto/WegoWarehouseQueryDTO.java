package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 * WEGO 开放接口请求参数
 */
@Data
@NoArgsConstructor
public class WegoWarehouseQueryDTO implements Serializable {

    /**
     * 查询仓库请求
     */
    @Data
    @NoArgsConstructor
    public static class QueryReqDTO {

        /**
         * WEGO 网关域名，例如 https://xxx.wegobusiness.com
         */
        @NotBlank(message = "WEGO域名不能为空")
        private String domain;

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
         * 附加业务参数，参与签名并一起发送
         */
        private Map<String, Object> bizParams;
    }
}
