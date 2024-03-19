package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CityBeanX {
    /**
     * id : TVgtUk9PVHVsdW0
     * name : Tulum
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
