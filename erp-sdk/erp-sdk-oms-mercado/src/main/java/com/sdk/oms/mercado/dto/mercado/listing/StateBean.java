package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class StateBean {
    /**
     * id : HK-HK
     * name : Hong Kong
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;

}
