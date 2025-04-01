package com.sdk.oms.mercadolocal.dto.mercadolocal.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
