package com.sdk.oms.tictok.dto.tiktok.shop;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class TikTokShopAuthDTO {
    /**
     * code : 0
     * data : {"shops":[{"cipher":"ROW_lQ9cEwAAAADLHlVuUFi_v-jD4goRMhED","code":"IDLCTFWMA3","id":"7495684755944606441","name":"SANDBOX7354929008369026821","region":"ID","seller_type":"LOCAL"}]}
     * message : Success
     * request_id : 202404090117285BA2E1A157309900659A
     */

    @SerializedName("code")
    private int code;
    @SerializedName("data")
    private DataBean data;
    @SerializedName("message")
    private String message;
    @SerializedName("request_id")
    private String requestId;
}
