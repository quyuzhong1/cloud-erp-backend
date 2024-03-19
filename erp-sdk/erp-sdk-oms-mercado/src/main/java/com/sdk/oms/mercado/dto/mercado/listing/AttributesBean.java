package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class AttributesBean {
    /**
     * id : BRAND
     * name : Brand
     * value_id : 2537728
     * value_name : Ulanzi
     * values : [{"id":"2537728","name":"Ulanzi","struct":null}]
     * value_type : string
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;
    @SerializedName("value_id")
    private String valueId;
    @SerializedName("value_name")
    private String valueName;
    @SerializedName("value_type")
    private String valueType;
    @SerializedName("values")
    private List<ValuesBeanX> values;

}
