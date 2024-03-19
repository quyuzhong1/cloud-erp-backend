package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class FiltersBean {
    /**
     * id : seller.id
     * name : seller ID
     * type : text
     * values : ["1511265855"]
     */

    @SerializedName("id")
    private String fid;
    @SerializedName("name")
    private String name;
    @SerializedName("type")
    private String type;
    @SerializedName("values")
    private List<String> values;

}
