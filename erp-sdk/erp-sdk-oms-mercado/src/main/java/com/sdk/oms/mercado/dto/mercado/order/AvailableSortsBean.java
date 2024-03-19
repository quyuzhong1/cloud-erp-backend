package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AvailableSortsBean {
    /**
     * id : date_desc
     * name : Date descending
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;

}
