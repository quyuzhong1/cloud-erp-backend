package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedbackBean {
    /**
     * purchase : null
     * sale : null
     */

    @JsonProperty("purchase")
    private Object purchase;
    @JsonProperty("sale")
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
