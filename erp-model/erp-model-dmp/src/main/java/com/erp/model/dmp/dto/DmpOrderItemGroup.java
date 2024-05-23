package com.erp.model.dmp.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class DmpOrderItemGroup {

    /**
     * 订单表id
     */
    private String orderId;

    /**
     * 商品id
     */
    private String itemId;

    /**
     * 平台sku
     */
    private String platformSku;
}
