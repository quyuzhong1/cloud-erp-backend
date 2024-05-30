package com.sdk.oms.tiktok.dto.tiktok.order.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailView {
    @JsonProperty("orders")
    private List<OrdersBean> orders;
}
