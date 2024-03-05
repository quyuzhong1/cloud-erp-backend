package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

public class TaxesBean {
    /**
     * amount : 0
     * currency_id : USD
     */

    @SerializedName("amount")
    private int amount;
    @SerializedName("currency_id")
    private String currencyId;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }
}
