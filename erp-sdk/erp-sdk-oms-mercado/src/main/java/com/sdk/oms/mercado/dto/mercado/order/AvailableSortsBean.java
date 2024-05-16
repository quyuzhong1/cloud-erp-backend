package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableSortsBean {
    /**
     * id : date_desc
     * name : Date descending
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
