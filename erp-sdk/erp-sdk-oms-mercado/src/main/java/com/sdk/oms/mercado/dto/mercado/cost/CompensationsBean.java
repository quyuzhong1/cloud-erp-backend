package com.sdk.oms.mercado.dto.mercado.cost;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompensationsBean {
    /**
     * amount : 1.45
     * reason : incorrect_dimensions
     * comment : Incorrect package dimensions
     */

    @JsonProperty("amount")
    private double amount;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("comment")
    private String comment;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
