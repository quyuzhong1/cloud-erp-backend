package com.sdk.oms.mercado.dto.mercado.cost;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CostDTO {


    /**
     * gross_amount : 5.46
     * currency_id : USD
     * receiver : {"user_id":139133205,"cost":0,"compensation":0,"save":0,"discounts":[{"rate":1,"type":"ratio","promoted_amount":5.46}],"compensations":[]}
     * senders : [{"user_id":1511265855,"cost":5.46,"compensation":0,"save":0,"discounts":[],"compensations":[{"amount":1.45,"reason":"incorrect_dimensions","comment":"Incorrect package dimensions"}]}]
     */

    @JsonProperty("gross_amount")
    private double grossAmount;
    @JsonProperty("currency_id")
    private String currencyId;
    @JsonProperty("receiver")
    private ReceiverBean receiver;
    @JsonProperty("senders")
    private List<SendersBean> senders;

    public double getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(double grossAmount) {
        this.grossAmount = grossAmount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public ReceiverBean getReceiver() {
        return receiver;
    }

    public void setReceiver(ReceiverBean receiver) {
        this.receiver = receiver;
    }

    public List<SendersBean> getSenders() {
        return senders;
    }

    public void setSenders(List<SendersBean> senders) {
        this.senders = senders;
    }
}
