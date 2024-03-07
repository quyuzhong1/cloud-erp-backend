package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

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

    public ItemBean getItem() {
        return item;
    }

    public void setItem(ItemBean item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getFullUnitPrice() {
        return fullUnitPrice;
    }

    public void setFullUnitPrice(BigDecimal fullUnitPrice) {
        this.fullUnitPrice = fullUnitPrice;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public Object getManufacturingDays() {
        return manufacturingDays;
    }

    public void setManufacturingDays(Object manufacturingDays) {
        this.manufacturingDays = manufacturingDays;
    }

    public BigDecimal getSaleFee() {
        return saleFee;
    }

    public void setSaleFee(BigDecimal saleFee) {
        this.saleFee = saleFee;
    }

    public BigDecimal getBaseExchangeRate() {
        return baseExchangeRate;
    }

    public void setBaseExchangeRate(BigDecimal baseExchangeRate) {
        this.baseExchangeRate = baseExchangeRate;
    }
}
