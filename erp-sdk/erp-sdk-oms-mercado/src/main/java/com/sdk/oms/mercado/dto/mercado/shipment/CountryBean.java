package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CountryBean {
    /**
     * id : HK
     * name : Hong Kong
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
