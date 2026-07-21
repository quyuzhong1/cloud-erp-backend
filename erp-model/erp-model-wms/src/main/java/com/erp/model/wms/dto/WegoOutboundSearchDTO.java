package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * WEGO 2C 出库单查询（interfaceType=2c.order.search）请求参数。
 *
 * <p>按 WEGO 出库单号列表精确查询，单次最多 100 条。</p>
 * <p>主要用于：</p>
 * <ul>
 *     <li>Handler {@code queryOutboundBill}：根据已知 WEGO 单号查询当前状态和物流跟踪号；</li>
 *     <li>下推创建出库单后轮询确认单号是否生效。</li>
 * </ul>
 *
 * <p>公共参数 {@code accessToken / interfaceType / sign} 由 SDK 统一拼装，
 * {@code secret} 仅参与本地签名计算，不会透传到 WEGO。</p>
 */
@Data
@NoArgsConstructor
public class WegoOutboundSearchDTO implements Serializable {

    /**
     * 2C 出库单查询请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchReqDTO implements Serializable {

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
         * WEGO 2C 出库单号列表（创建成功后由 WEGO 返回的单号，
         * 存入 third_warehouse_delivery.shipping_order_no），单次最多 100 条
         */
        @NotEmpty(message = "出库单号列表不能为空")
        @Size(max = 100, message = "单次最多查询100条")
        private List<String> noList;
    }
}
