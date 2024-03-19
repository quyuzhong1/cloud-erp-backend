package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class LeadTimeBean {
    /**
     * option_id : 1338705174
     * shipping_method : {"id":509450,"type":"standard","name":"Estándar a domicilio","deliver_to":"address"}
     * currency_id : USD
     * cost : 0
     * list_cost : 5.46
     * cost_type : free
     * service_id : 628333
     * estimated_delivery_time : {"type":"known_frame","date":"2024-03-01T00:00:00.000-06:00","unit":"hour","offset":{"date":"2024-03-14T00:00:00.000-06:00","shipping":216},"time_frame":{"from":"","to":""},"pay_before":"2024-02-19T10:00:00.000-06:00","shipping":168,"handling":72,"schedule":null}
     */

    @JsonProperty("option_id")
    private int optionId;
    @JsonProperty("shipping_method")
    private ShippingMethodBean shippingMethod;
    @JsonProperty("currency_id")
    private String currencyId;
    @JsonProperty("cost")
    private BigDecimal cost;
    @JsonProperty("list_cost")
    private BigDecimal listCost;
    @JsonProperty("cost_type")
    private String costType;
    @JsonProperty("service_id")
    private int serviceId;
    @JsonProperty("estimated_delivery_time")
    private EstimatedDeliveryTimeBean estimatedDeliveryTime;

    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public ShippingMethodBean getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(ShippingMethodBean shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getListCost() {
        return listCost;
    }

    public void setListCost(BigDecimal listCost) {
        this.listCost = listCost;
    }

    public String getCostType() {
        return costType;
    }

    public void setCostType(String costType) {
        this.costType = costType;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public EstimatedDeliveryTimeBean getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(EstimatedDeliveryTimeBean estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }
}
