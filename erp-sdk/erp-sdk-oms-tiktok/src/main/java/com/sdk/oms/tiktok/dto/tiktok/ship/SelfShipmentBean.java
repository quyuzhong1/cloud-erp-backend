package com.sdk.oms.tiktok.dto.tiktok.ship;

import com.google.gson.annotations.SerializedName;

public class SelfShipmentBean {
    /**
     * shipping_provider_id : 6617675021119438849
     * tracking_number : JX12345
     */

    @SerializedName("shipping_provider_id")
    private String shippingProviderId;
    @SerializedName("tracking_number")
    private String trackingNumber;

    public String getShippingProviderId() {
        return shippingProviderId;
    }

    public void setShippingProviderId(String shippingProviderId) {
        this.shippingProviderId = shippingProviderId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}
