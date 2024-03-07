package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;

public class OffsetBean {
    /**
     * date : 2024-03-14T00:00:00.000-06:00
     * shipping : 216
     */

    @SerializedName("date")
    private String date;
    @SerializedName("shipping")
    private int shipping;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getShipping() {
        return shipping;
    }

    public void setShipping(int shipping) {
        this.shipping = shipping;
    }
}
