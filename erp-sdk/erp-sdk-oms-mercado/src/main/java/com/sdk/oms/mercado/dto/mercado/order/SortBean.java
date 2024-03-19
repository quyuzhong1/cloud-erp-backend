package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SortBean {
    /**
     * id : date_asc
     * name : Date ascending
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;

}
