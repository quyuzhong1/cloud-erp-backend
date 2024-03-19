package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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
