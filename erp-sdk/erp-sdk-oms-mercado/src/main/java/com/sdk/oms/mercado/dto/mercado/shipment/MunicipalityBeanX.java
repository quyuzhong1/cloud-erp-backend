package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MunicipalityBeanX {
    /**
     * id : null
     * name : Tulum
     */

    @JsonProperty("id")
    private Object fid;
    @JsonProperty("name")
    private String name;

}
