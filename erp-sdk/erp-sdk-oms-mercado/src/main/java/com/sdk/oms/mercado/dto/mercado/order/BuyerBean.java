package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuyerBean {
    /**
     * id : 139133205
     */

    @JsonProperty("id")
    private int fid;

}
