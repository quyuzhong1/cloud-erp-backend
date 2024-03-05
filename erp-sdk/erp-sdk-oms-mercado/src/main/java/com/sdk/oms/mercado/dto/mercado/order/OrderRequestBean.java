package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

public class OrderRequestBean {
    /**
     * return : null
     * change : null
     */

    @SerializedName("return")
    private Object returnX;
    @SerializedName("change")
    private Object change;

    public Object getReturnX() {
        return returnX;
    }

    public void setReturnX(Object returnX) {
        this.returnX = returnX;
    }

    public Object getChange() {
        return change;
    }

    public void setChange(Object change) {
        this.change = change;
    }
}
