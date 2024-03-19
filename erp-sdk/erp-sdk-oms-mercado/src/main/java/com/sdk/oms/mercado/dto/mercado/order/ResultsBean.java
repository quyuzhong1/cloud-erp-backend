package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class ResultsBean {
    @SerializedName("id")
    private long fid;
    @SerializedName("buyer")
    private BuyerBean buyer;
    @SerializedName("config")
    private ConfigBean config;
    @SerializedName("shipment")
    private ShipmentBean shipment;
    @SerializedName("orders")
    private List<OrdersBean> orders;

}
