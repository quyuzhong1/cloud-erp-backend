package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OffsetBean {
    /**
     * date : 2024-03-14T00:00:00.000-06:00
     * shipping : 216
     */

    @JsonProperty("date")
    private String date;
    @JsonProperty("shipping")
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
