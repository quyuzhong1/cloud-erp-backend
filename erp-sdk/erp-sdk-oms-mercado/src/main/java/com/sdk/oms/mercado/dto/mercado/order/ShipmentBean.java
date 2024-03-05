package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShipmentBean {
    /**
     * id : 43116658829
     * payments : []
     */

    @SerializedName("id")
    private long id;
    @SerializedName("payments")
    private List<?> payments;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<?> getPayments() {
        return payments;
    }

    public void setPayments(List<?> payments) {
        this.payments = payments;
    }
}
