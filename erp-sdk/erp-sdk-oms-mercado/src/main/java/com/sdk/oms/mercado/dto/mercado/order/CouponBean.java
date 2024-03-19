package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CouponBean {
    /**
     * id : null
     * amount : 36.27
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("amount")
    private double amount;

}
