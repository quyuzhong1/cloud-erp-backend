package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AvailableOrdersBean {
    /**
     * id : stop_time_asc
     * name : Order by stop time ascending
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
