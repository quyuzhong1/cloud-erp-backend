package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SellerAddressBeanX {
    /**
     * city : {"id":"SEstSEtLd2FpIFRzaW5n","name":"Kwai Tsing"}
     * state : {"id":"HK-HK","name":"Hong Kong"}
     * country : {"id":"HK","name":"Hong Kong"}
     * id : 1332219696
     */

    @JsonProperty("city")
    private CityBean city;
    @JsonProperty("state")
    private CityBean state;
    @JsonProperty("country")
    private CityBean country;
    @JsonProperty("id")
    private int fid;

}
