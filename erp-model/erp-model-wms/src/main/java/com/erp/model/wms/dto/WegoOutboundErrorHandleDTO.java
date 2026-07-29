package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * WEGO 2C 订单异常出库取消（interfaceType=2c.order.errorHandle）请求参数。
 *
 * <p>仅适用于出库单状态为「出库异常」「提交失败」等异常态的取消；
 * 取消后订单将被锁定并扣减总库存，如需继续发货须重新创建出库单。</p>
 *
 * <p>公共参数 {@code accessToken / interfaceType / sign} 由 SDK 统一拼装，
 * {@code secret} 仅参与本地签名计算，不会透传到 WEGO。</p>
 */
@Data
@NoArgsConstructor
public class WegoOutboundErrorHandleDTO implements Serializable {

    /**
     * 2C 订单异常出库取消请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorHandleReqDTO implements Serializable {

        /**
         * WEGO accessToken（取自 overseas_provider.auth_json.appToken）
         */
        @NotBlank(message = "accessToken不能为空")
        private String accessToken;

        /**
         * WEGO secret（仅参与本地签名，不会透传到第三方）
         */
        @NotBlank(message = "secret不能为空")
        private String secret;

        /**
         * WEGO 2C 出库单号
         */
        @NotBlank(message = "WEGO出库单号不能为空")
        private String no;
    }
}
