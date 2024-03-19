package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CityBean {
    /**
     * id : SEstSEtLd2FpIFRzaW5n
     * name : Kwai Tsing
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;

}
