package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ValuesBeanX {
    /**
     * id : 2537728
     * name : Ulanzi
     * struct : null
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;
    @SerializedName("struct")
    private Object struct;

}
