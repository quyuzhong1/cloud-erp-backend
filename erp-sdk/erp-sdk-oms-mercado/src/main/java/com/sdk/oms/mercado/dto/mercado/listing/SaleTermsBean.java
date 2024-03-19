package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SaleTermsBean {
    /**
     * id : WARRANTY_TIME
     * name : Warranty time
     * value_id : null
     * value_name : 1 months
     * value_struct : {"number":1,"unit":"months"}
     * values : [{"id":null,"name":"1 months","struct":{"number":1,"unit":"months"}}]
     * value_type : number_unit
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("value_id")
    private Object valueId;
    @JsonProperty("value_name")
    private String valueName;
    @JsonProperty("value_struct")
    private ValueStructBean valueStruct;
    @JsonProperty("value_type")
    private String valueType;
    @JsonProperty("values")
    private List<ValuesBean> values;

}
