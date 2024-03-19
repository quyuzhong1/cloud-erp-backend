package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StateBeanX {
    /**
     * id : MX-ROO
     * name : Quintana Roo
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
