package com.sdk.oms.tiktok.dto.tiktok.split;

import com.google.gson.annotations.SerializedName;

public class SplitAttributesDTO {

    /**
     * code : 0
     * data : {"split_attributes":[{"can_split":false,"order_id":"578722862857422858","reason":"split same sku in Multi Package not allow"}]}
     * message : Success
     * request_id : 2024041009154567059C72FA1C7900AB57
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
