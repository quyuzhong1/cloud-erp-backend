package com.sdk.oms.tiktok.dto.tiktok.ship;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShipOrderUSParam {

    /**
     * tracking_number : 789
     * shipping_provider_id : 7202858143536121601
     * order_line_item_ids : ["578720468777273354"]
     */

    @SerializedName("tracking_number")
    private String trackingNumber;
    @SerializedName("shipping_provider_id")
    private String shippingProviderId;
    @SerializedName("order_line_item_ids")
    private List<String> orderLineItemIds;

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getShippingProviderId() {
        return shippingProviderId;
    }

    public void setShippingProviderId(String shippingProviderId) {
        this.shippingProviderId = shippingProviderId;
    }

    public List<String> getOrderLineItemIds() {
        return orderLineItemIds;
    }

    public void setOrderLineItemIds(List<String> orderLineItemIds) {
        this.orderLineItemIds = orderLineItemIds;
    }
}
