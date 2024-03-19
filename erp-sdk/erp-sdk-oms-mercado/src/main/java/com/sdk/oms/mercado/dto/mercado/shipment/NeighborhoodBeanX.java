package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NeighborhoodBeanX {
    /**
     * id : null
     * name : Guerra de Castas
     */

    @JsonProperty("id")
    private Object fid;
    @JsonProperty("name")
    private String name;

}
