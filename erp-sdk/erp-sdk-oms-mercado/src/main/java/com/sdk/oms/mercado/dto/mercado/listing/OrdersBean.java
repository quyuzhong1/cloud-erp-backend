package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class OrdersBean {
    /**
     * id : stop_time_asc
     * name : Order by stop time ascending
     */

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;

}
