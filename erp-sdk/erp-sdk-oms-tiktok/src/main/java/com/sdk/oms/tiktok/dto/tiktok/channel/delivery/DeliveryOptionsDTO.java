package com.sdk.oms.tiktok.dto.tiktok.channel.delivery;

import com.google.gson.annotations.SerializedName;

public class DeliveryOptionsDTO {

    /**
     * code : 0
     * data : {"delivery_options":[{"description":"only for testflag package with the region ID","dimension_limit":{"max_height":160,"max_length":160,"max_width":160,"unit":"CM"},"id":"6956553057215710977","name":"ecom_logistics_type_Standard","type":"STANDARD","weight_limit":{"max_weight":100000,"min_weight":0,"unit":"GRAM"}}]}
     * message : Success
     * request_id : 2024041507102973D6D7939D784500A458
     */

    @SerializedName("code")
    private int code;
    @SerializedName("data")
    private DataBean data;
    @SerializedName("message")
    private String message;
    @SerializedName("request_id")
    private String requestId;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
