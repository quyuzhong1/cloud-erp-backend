package com.sdk.oms.tiktok.dto.tiktok.channel.provider;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataBean {
    @SerializedName("shipping_providers")
    private List<ShippingProvidersBean> shippingProviders;

    public List<ShippingProvidersBean> getShippingProviders() {
        return shippingProviders;
    }

    public void setShippingProviders(List<ShippingProvidersBean> shippingProviders) {
        this.shippingProviders = shippingProviders;
    }
}
