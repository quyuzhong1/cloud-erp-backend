package com.sdk.oms.mercadolocal.dto.mercadolocal.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AvailableOrdersBean {
    /**
     * id : stop_time_asc
     * name : Order by stop time ascending
     */

    @JsonProperty("id")
    private Object fid;
    @JsonProperty("name")
    private String name;

}
