package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class StateBeanX {
    /**
     * id : MX-ROO
     * name : Quintana Roo
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;

}
