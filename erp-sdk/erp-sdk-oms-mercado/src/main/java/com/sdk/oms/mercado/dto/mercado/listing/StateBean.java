package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateBean {
    /**
     * id : HK-HK
     * name : Hong Kong
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
