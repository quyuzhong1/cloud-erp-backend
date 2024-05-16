package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SortBean {
    /**
     * id : date_asc
     * name : Date ascending
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
