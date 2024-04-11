package com.sdk.oms.tictok.dto.tiktok.split;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SplitAttributesBean {
    /**
     * can_split : false
     * order_id : 578722862857422858
     * reason : split same sku in Multi Package not allow
     */

    @SerializedName("can_split")
    private Boolean canSplit;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("reason")
    private String reason;
}
