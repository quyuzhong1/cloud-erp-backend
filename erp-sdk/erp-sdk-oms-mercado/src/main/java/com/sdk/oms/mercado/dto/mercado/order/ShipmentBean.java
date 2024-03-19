package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ShipmentBean {
    /**
     * id : 43116658829
     * payments : []
     */

    @JsonProperty("id")
    private long fid;
    @JsonProperty("payments")
    private List<?> payments;

}
