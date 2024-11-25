package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class SellerNextActionResponseBean {
    /**
     * action : SELLER_RESPOND_REFUND
     * deadline : 1690554680
     */

    @SerializedName("action")
    private String action;
    @SerializedName("deadline")
    private int deadline;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }
}
