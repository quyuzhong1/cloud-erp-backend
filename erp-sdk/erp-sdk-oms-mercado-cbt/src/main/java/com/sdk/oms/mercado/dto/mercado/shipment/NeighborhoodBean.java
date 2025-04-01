package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeighborhoodBean {
    /**
     * id : null
     * name : null
     */

    @JsonProperty("id")
    private Object fid;
    @JsonProperty("name")
    private Object name;
}
