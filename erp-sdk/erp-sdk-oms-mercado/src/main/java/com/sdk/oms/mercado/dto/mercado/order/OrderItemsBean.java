package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemsBean {
    /**
     * item : {"id":"MLM2802963514","title":"Ulanzi R099 Kit De Montaje Con Clip Para Cámara Gopro","category_id":"MLM127873","variation_id":null,"seller_custom_field":null,"variation_attributes":[],"warranty":"Garantía del vendedor: 1 meses","condition":"new","seller_sku":"2993+1764A+0605","parent_item_id":"CBT1908713864"}
     * quantity : 1
     * unit_price : 21.2
     * full_unit_price : 21.2
     * currency_id : USD
     * manufacturing_days : null
     * sale_fee : 3.18
     * base_exchange_rate : 17.11
     */

    @JsonProperty("item")
    private ItemBean item;
    @JsonProperty("quantity")
    private int quantity;
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    @JsonProperty("full_unit_price")
    private BigDecimal fullUnitPrice;
    @JsonProperty("currency_id")
    private String currencyId;
    @JsonProperty("manufacturing_days")
    private Object manufacturingDays;
    @JsonProperty("sale_fee")
    private BigDecimal saleFee;
    @JsonProperty("base_exchange_rate")
    private BigDecimal baseExchangeRate;

}
