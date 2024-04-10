package com.sdk.oms.tictok.dto.tiktok.split;

import com.google.gson.annotations.SerializedName;

public class SplitAttributesBean {
    /**
     * can_split : false
     * order_id : 578722862857422858
     * reason : split same sku in Multi Package not allow
     */

    @SerializedName("can_split")
    private boolean canSplit;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("reason")
    private String reason;

    public boolean isCanSplit() {
        return canSplit;
    }

    public void setCanSplit(boolean canSplit) {
        this.canSplit = canSplit;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
