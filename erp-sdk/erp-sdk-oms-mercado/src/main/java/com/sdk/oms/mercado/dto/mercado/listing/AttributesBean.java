package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AttributesBean {
    /**
     * id : BRAND
     * name : Brand
     * value_id : 59387
     * value_name : Xiaomi
     */

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("value_id")
    private String valueId;
    @SerializedName("value_name")
    private String valueName;

}
