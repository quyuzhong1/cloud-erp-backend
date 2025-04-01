package com.sdk.oms.mercadolocal.dto.mercadolocal.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityBean {
    /**
     * id : SEstSEtLd2FpIFRzaW5n
     * name : Kwai Tsing
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
