package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CountryBean {
    /**
     * id : HK
     * name : Hong Kong
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;

}
