package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class OrderRequestBean {
    /**
     * return : null
     * change : null
     */

    @SerializedName("return")
    private Object returnX;
    @SerializedName("change")
    private Object change;

}
