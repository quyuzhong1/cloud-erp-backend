package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequestBean {
    /**
     * return : null
     * change : null
     */

    @JsonProperty("return")
    private Object returnX;
    @JsonProperty("change")
    private Object change;

}
