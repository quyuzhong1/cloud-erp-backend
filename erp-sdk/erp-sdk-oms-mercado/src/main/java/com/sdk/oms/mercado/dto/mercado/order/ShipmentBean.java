package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class ShipmentBean {
    /**
     * id : 43116658829
     * payments : []
     */

    @SerializedName("id")
    private long fid;
    @SerializedName("payments")
    private List<?> payments;

}
