package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

public class CouponBean {
    /**
     * id : null
     * amount : 36.27
     */

    @SerializedName("id")
    private Object id;
    @SerializedName("amount")
    private double amount;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
