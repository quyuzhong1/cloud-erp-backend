package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private long optionId;
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

}
