package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemsBean {


    @SerializedName("item")
    private ItemBean item;
    @SerializedName("quantity")
    private int quantity;
    @SerializedName("unit_price")
    private BigDecimal unitPrice;
    @SerializedName("full_unit_price")
    private BigDecimal fullUnitPrice;
    @SerializedName("currency_id")
    private String currencyId;
    @SerializedName("manufacturing_days")
    private Object manufacturingDays;
    @SerializedName("sale_fee")
    private BigDecimal saleFee;
    @SerializedName("base_exchange_rate")
    private BigDecimal baseExchangeRate;

}
