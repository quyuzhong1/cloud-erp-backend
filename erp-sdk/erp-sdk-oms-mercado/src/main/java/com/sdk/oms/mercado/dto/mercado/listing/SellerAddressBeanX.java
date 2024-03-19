package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SellerAddressBeanX {
    /**
     * city : {"id":"SEstSEtLd2FpIFRzaW5n","name":"Kwai Tsing"}
     * state : {"id":"HK-HK","name":"Hong Kong"}
     * country : {"id":"HK","name":"Hong Kong"}
     * id : 1332219696
     */

    @SerializedName("city")
    private CityBean city;
    @SerializedName("state")
    private CityBean state;
    @SerializedName("country")
    private CityBean country;
    @SerializedName("id")
    private int fid;

}
