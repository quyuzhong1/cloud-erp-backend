package com.sdk.oms.mercado.dto;

import lombok.Data;

@Data
public class MercadoShipOrderDTO {
    /**
     * 平台发货详情id
     */
    private String shipmentId;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 运单号
     */
    private String trackingId;
    /**
     * 运单url
     */
    private String trackingUrl;
    /**
     * 承运商
     */
    private String carrier;
}
