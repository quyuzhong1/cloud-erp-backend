package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemsBean {
    /**
     * item : {"id":"MLB5283694678","title":"Ulanzi J12 Lavalier Microfone Sem Fio Para iPhone iPad","category_id":"MLB439409","variation_id":186902719495,"seller_custom_field":null,"global_price":null,"net_weight":null,"variation_attributes":[{"name":"Cor","id":"COLOR","value_id":"52049","value_name":"Preto"}],"warranty":"Garantia de fábrica: 90 dias","condition":"new","seller_sku":"2885"}
     * quantity : 1
     * unit_price : 170
     * full_unit_price : 170
     * currency_id : BRL
     * manufacturing_days : 5
     * picked_quantity : null
     * requested_quantity : {"measure":"unit","value":1}
     * sale_fee : 22.1
     * listing_type_id : gold_special
     * base_exchange_rate : null
     * base_currency_id : null
     * bundle : null
     * element_id : 1
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
    private int manufacturingDays;
    @JsonProperty("picked_quantity")
    private Object pickedQuantity;
    @JsonProperty("requested_quantity")
    private RequestedQuantityBean requestedQuantity;
    @JsonProperty("sale_fee")
    private double saleFee;
    @JsonProperty("listing_type_id")
    private String listingTypeId;
    @JsonProperty("base_exchange_rate")
    private BigDecimal baseExchangeRate;
    @JsonProperty("base_currency_id")
    private Object baseCurrencyId;
    @JsonProperty("bundle")
    private Object bundle;
    @JsonProperty("element_id")
    private int elementId;

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

    public int getManufacturingDays() {
        return manufacturingDays;
    }

    public void setManufacturingDays(int manufacturingDays) {
        this.manufacturingDays = manufacturingDays;
    }

    public Object getPickedQuantity() {
        return pickedQuantity;
    }

    public void setPickedQuantity(Object pickedQuantity) {
        this.pickedQuantity = pickedQuantity;
    }

    public RequestedQuantityBean getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(RequestedQuantityBean requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public double getSaleFee() {
        return saleFee;
    }

    public void setSaleFee(double saleFee) {
        this.saleFee = saleFee;
    }

    public String getListingTypeId() {
        return listingTypeId;
    }

    public void setListingTypeId(String listingTypeId) {
        this.listingTypeId = listingTypeId;
    }

    public BigDecimal getBaseExchangeRate() {
        return baseExchangeRate;
    }

    public void setBaseExchangeRate(BigDecimal baseExchangeRate) {
        this.baseExchangeRate = baseExchangeRate;
    }

    public Object getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public void setBaseCurrencyId(Object baseCurrencyId) {
        this.baseCurrencyId = baseCurrencyId;
    }

    public Object getBundle() {
        return bundle;
    }

    public void setBundle(Object bundle) {
        this.bundle = bundle;
    }

    public int getElementId() {
        return elementId;
    }

    public void setElementId(int elementId) {
        this.elementId = elementId;
    }
}
