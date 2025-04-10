package com.sdk.oms.mercadolocal.dto;

import lombok.Data;

@Data
public class MercadoInvoiceDTO {
    /**
     * 平台发货详情id
     */
    private String shipmentId;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 发票xml内容
     */
    private String xmlContent;
}
