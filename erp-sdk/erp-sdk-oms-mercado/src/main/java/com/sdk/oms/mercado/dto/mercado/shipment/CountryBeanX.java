package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CountryBeanX {
    /**
     * id : MX
     * name : Mexico
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;

}
