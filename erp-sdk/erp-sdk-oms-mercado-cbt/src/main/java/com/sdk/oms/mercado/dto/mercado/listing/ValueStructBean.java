package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueStructBean {
    /**
     * number : 1
     * unit : months
     */

    @JsonProperty("number")
    private int number;
    @JsonProperty("unit")
    private String unit;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
