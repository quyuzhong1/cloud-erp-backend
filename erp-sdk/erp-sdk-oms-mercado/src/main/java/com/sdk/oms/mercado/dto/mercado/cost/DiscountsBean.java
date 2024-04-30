package com.sdk.oms.mercado.dto.mercado.cost;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscountsBean {
    /**
     * rate : 1
     * type : ratio
     * promoted_amount : 5.46
     */

    @JsonProperty("rate")
    private int rate;
    @JsonProperty("type")
    private String type;
    @JsonProperty("promoted_amount")
    private double promotedAmount;

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPromotedAmount() {
        return promotedAmount;
    }

    public void setPromotedAmount(double promotedAmount) {
        this.promotedAmount = promotedAmount;
    }
}
