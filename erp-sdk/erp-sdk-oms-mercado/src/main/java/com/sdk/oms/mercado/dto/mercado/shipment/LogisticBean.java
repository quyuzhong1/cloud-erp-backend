package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;

public class LogisticBean {
    /**
     * mode : me2
     * type : drop_off
     * direction : forward
     */

    @SerializedName("mode")
    private String mode;
    @SerializedName("type")
    private String type;
    @SerializedName("direction")
    private String direction;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
