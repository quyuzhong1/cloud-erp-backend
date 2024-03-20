package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributesBean {
    /**
     * id : BRAND
     * name : Brand
     * value_id : 2537728
     * value_name : Ulanzi
     * values : [{"id":"2537728","name":"Ulanzi","struct":null}]
     * value_type : string
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("value_id")
    private String valueId;
    @JsonProperty("value_name")
    private String valueName;
    @JsonProperty("value_type")
    private String valueType;
    @JsonProperty("values")
    private List<ValuesBeanX> values;

}
