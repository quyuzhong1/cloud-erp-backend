package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * WEGO 派送渠道（transport.get）开放接口请求参数。
 * <p>
 * 公共参数 {@code accessToken / interfaceType / sign} 在 SDK 内统一拼装，
 * 业务参数（warehouseBusiness / warehouseCode / transportationType）通过
 * {@link QueryReqDTO#bizParams} 透传，参与签名与请求体。
 */
@Data
@NoArgsConstructor
public class WegoTransportQueryDTO implements Serializable {

    /**
     * WEGO transport.get 业务参数 key：海外仓供应商
     */
    public static final String BIZ_KEY_WAREHOUSE_BUSINESS = "warehouseBusiness";

    /**
     * WEGO transport.get 业务参数 key：仓库代码
     */
    public static final String BIZ_KEY_WAREHOUSE_CODE = "warehouseCode";

    /**
     * WEGO transport.get 业务参数 key：派送渠道类型 (0:2C, 1:2B)
     */
    public static final String BIZ_KEY_TRANSPORTATION_TYPE = "transportationType";

    /**
     * 查询派送渠道请求
     */
    @Data
    @NoArgsConstructor
    public static class QueryReqDTO {

        /**
         * WEGO accessToken
         */
        private String accessToken;

        /**
         * WEGO secret（用于本地签名，不会发给第三方）
         */
        private String secret;

        /**
         * 附加业务参数，参与签名并一起发送。可包含：
         * <ul>
         *     <li>{@link #BIZ_KEY_WAREHOUSE_BUSINESS}：海外仓供应商，可选</li>
         *     <li>{@link #BIZ_KEY_WAREHOUSE_CODE}：仓库代码，可选</li>
         *     <li>{@link #BIZ_KEY_TRANSPORTATION_TYPE}：派送渠道类型 0:2C / 1:2B，可选</li>
         * </ul>
         */
        private Map<String, Object> bizParams;
    }
}
