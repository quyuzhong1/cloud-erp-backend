package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * WEGO 2C 出库单取消/截单（interfaceType=2c.order.intercept）请求参数。
 *
 * <p>截单说明（来自 WEGO 官方文档）：</p>
 * <ul>
 *     <li>「已提交」前的订单可直接取消；</li>
 *     <li>「拣货中、已拣货」的订单也可取消，视为仓内拦截，产生操作费，无物流费用；</li>
 *     <li>「已出库」订单无法线上拦截，只可联系客服线下通知物流公司拦截。</li>
 * </ul>
 *
 * <p>截单成功响应 {@code success=true}，失败响应 {@code success=false}（如 errorCode=2004 截单失败）。</p>
 *
 * <p>公共参数 {@code accessToken / interfaceType / sign} 由 SDK 统一拼装，
 * {@code secret} 仅参与本地签名计算，不会透传到 WEGO。</p>
 */
@Data
@NoArgsConstructor
public class WegoOutboundInterceptDTO implements Serializable {

    /**
     * 2C 出库单截单请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InterceptReqDTO implements Serializable {

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
         * WEGO 2C 出库单号（创建成功后由 WEGO 返回，存入 third_warehouse_delivery.shipping_order_no）
         */
        @NotBlank(message = "WEGO出库单号不能为空")
        private String no;
    }
}
