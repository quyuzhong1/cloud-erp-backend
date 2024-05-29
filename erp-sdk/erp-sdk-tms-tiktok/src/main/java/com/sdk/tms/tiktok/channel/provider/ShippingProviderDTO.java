package com.sdk.tms.tiktok.channel.provider;

import com.google.gson.annotations.SerializedName;

public class ShippingProviderDTO {

    /**
     * code : 0
     * data : {"shipping_providers":[{"id":"6617675021119438849","name":"TT Virtual JNT express"},{"id":"7202858143536121601","name":"TT Virtual J&T Cargo"}]}
     * message : Success
     * request_id : 202404150731279A3801DF0250C40129A8
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
