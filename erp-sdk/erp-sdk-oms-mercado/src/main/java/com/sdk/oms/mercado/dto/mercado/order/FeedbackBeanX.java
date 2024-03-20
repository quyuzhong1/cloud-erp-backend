package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedbackBeanX {
    /**
     * sale : null
     * purchase : null
     */

    @JsonProperty("sale")
    private String sale;
    @JsonProperty("purchase")
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
