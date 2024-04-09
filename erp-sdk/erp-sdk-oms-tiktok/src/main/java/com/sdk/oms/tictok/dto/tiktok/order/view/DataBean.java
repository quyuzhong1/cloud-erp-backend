package com.sdk.oms.tictok.dto.tiktok.order.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataBean {
    @JsonProperty("orders")
    private List<OrdersBean> orders;
}
