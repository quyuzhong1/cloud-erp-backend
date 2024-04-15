package com.sdk.tms.tiktok.channel.delivery;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataBean {
    @SerializedName("delivery_options")
    private List<DeliveryOptionsBean> deliveryOptions;

    public List<DeliveryOptionsBean> getDeliveryOptions() {
        return deliveryOptions;
    }

    public void setDeliveryOptions(List<DeliveryOptionsBean> deliveryOptions) {
        this.deliveryOptions = deliveryOptions;
    }
}
