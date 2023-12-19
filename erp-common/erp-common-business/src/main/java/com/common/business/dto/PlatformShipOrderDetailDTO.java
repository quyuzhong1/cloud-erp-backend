package com.common.business.dto;

import lombok.Data;

@Data
public class PlatformShipOrderDetailDTO {
    /**
     * 平台sku
     */
    private String platformSkuNo;
    /**
     * 平台产品id
     */
    private String platformSpuNo;
    /**
     * 发货数量
     */
    private String qty;
}
