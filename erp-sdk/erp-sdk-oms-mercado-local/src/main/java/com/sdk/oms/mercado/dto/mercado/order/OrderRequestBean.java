package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequestBean {
    /**
     * change : null
     * return : null
     */

    @JsonProperty("change")
    private Object change;
    @JsonProperty("return")
    private Object returnX;

    public Object getChange() {
        return change;
    }

    public void setChange(Object change) {
        this.change = change;
    }

    public Object getReturnX() {
        return returnX;
    }

    public void setReturnX(Object returnX) {
        this.returnX = returnX;
    }
}
