package com.sdk.oms.tiktok.dto.tiktok.ship;

import com.google.gson.annotations.SerializedName;

public class ShipOrderUS {

    /**
     * code : 0
     * data : {"order_id":"32131324123321","order_line_item_ids":["31322412312312"],"package_id":"32141235124234","warning":{"message":"match more than one provider"}}
     * message : Success
     * request_id : 202203070749000101890810281E8C70B7
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
