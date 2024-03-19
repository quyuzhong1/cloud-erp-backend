package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ValuesBean {
    /**
     * id : null
     * name : 1 months
     * struct : {"number":1,"unit":"months"}
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;
    @SerializedName("struct")
    private StructBean struct;

}
