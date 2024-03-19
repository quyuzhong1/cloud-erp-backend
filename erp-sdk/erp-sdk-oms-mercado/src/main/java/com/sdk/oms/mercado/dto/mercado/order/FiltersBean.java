package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FiltersBean {
    /**
     * id : seller.id
     * name : seller ID
     * type : text
     * values : ["1511265855"]
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private String type;
    @JsonProperty("values")
    private List<String> values;

}
