package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * WEGO 入库订单取消（interfaceType=inorder.cancel）请求参数
 *
 * <p>WEGO 服务端按 {@link CancelReqDTO#getNo()} 定位入库单并执行取消，
 * 取消成功后无法再恢复，业务侧需保证调用前已切换为本地的取消状态。</p>
 *
 * <p>公共参数 {@code accessToken / interfaceType / sign} 在 SDK 内统一拼装，
 * {@code secret} 仅参与本地签名计算，不会透传到 WEGO。</p>
 */
@Data
@NoArgsConstructor
public class WegoInOrderCancelDTO implements Serializable {

    /**
     * 入库单取消请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancelReqDTO {

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
         * WEGO 入库单号
         */
        @NotBlank(message = "no不能为空")
        private String no;
    }
}
