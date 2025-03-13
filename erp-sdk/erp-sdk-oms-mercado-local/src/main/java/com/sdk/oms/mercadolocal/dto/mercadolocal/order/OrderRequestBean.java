package com.sdk.oms.mercadolocal.dto.mercadolocal.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
