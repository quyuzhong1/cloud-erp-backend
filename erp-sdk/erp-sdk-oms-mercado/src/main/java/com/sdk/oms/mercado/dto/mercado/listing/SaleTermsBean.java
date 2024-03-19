package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
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

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;
    @SerializedName("value_id")
    private Object valueId;
    @SerializedName("value_name")
    private String valueName;
    @SerializedName("value_struct")
    private ValueStructBean valueStruct;
    @SerializedName("value_type")
    private String valueType;
    @SerializedName("values")
    private List<ValuesBean> values;

}
