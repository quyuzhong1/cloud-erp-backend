package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ValuesBean {
    /**
     * id : null
     * name : 1 months
     * struct : {"number":1,"unit":"months"}
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("struct")
    private StructBean struct;

}
