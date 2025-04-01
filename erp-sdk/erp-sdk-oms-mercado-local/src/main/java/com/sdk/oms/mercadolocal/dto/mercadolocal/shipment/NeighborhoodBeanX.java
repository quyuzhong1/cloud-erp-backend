package com.sdk.oms.mercadolocal.dto.mercadolocal.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
