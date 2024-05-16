package com.sdk.oms.tiktok.dto.tiktok.ship;

import com.google.gson.annotations.SerializedName;

public class WarningBean {
    /**
     * message : match more than one provider
     */

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
