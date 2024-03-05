package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

public class FeedbackBeanX {
    /**
     * sale : null
     * purchase : null
     */

    @SerializedName("sale")
    private Object sale;
    @SerializedName("purchase")
    private Object purchase;

    public Object getSale() {
        return sale;
    }

    public void setSale(Object sale) {
        this.sale = sale;
    }

    public Object getPurchase() {
        return purchase;
    }

    public void setPurchase(Object purchase) {
        this.purchase = purchase;
    }
}
