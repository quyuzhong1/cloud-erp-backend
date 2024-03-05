package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

public class FeedbackBean {
    /**
     * purchase : null
     * sale : null
     */

    @SerializedName("purchase")
    private Object purchase;
    @SerializedName("sale")
    private Object sale;

    public Object getPurchase() {
        return purchase;
    }

    public void setPurchase(Object purchase) {
        this.purchase = purchase;
    }

    public Object getSale() {
        return sale;
    }

    public void setSale(Object sale) {
        this.sale = sale;
    }
}
