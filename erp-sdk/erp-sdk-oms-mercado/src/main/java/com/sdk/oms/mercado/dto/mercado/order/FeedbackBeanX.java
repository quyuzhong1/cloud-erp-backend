package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

public class FeedbackBeanX {
    /**
     * sale : null
     * purchase : null
     */

    @SerializedName("sale")
    private String sale;
    @SerializedName("purchase")
    private String purchase;

    public String getSale() {
        return sale;
    }

    public void setSale(String sale) {
        this.sale = sale;
    }

    public String getPurchase() {
        return purchase;
    }

    public void setPurchase(String purchase) {
        this.purchase = purchase;
    }
}
