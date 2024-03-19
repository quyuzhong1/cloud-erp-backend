package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CouponBean {
    /**
     * id : null
     * amount : 36.27
     */

    @SerializedName("id")
    private Object fid;
    @SerializedName("amount")
    private double amount;

}
