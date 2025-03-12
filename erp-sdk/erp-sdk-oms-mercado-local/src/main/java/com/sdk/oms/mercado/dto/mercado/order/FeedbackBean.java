package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedbackBean {
    /**
     * buyer : null
     * seller : null
     */

    @JsonProperty("buyer")
    private Object buyer;
    @JsonProperty("seller")
    private Object seller;

    public Object getBuyer() {
        return buyer;
    }

    public void setBuyer(Object buyer) {
        this.buyer = buyer;
    }

    public Object getSeller() {
        return seller;
    }

    public void setSeller(Object seller) {
        this.seller = seller;
    }
}
