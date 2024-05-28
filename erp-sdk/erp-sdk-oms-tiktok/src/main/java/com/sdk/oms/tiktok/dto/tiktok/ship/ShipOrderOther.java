package com.sdk.oms.tiktok.dto.tiktok.ship;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ShipOrderOther {

    /**
     * code : 0
     * data : {}
     * message : Success
     * request_id : 202203070749000101890810281E8C70B7
     */

    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    @SerializedName("request_id")
    private String requestId;
}
