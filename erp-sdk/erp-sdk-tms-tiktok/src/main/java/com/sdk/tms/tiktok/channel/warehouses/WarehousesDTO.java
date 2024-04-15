package com.sdk.tms.tiktok.channel.warehouses;

import com.google.gson.annotations.SerializedName;

public class WarehousesDTO {

    /**
     * code : 0
     * data : {"warehouses":[{"address":{"city":"South Jakarta City","contact_person":"TikTok Shop Partner Center","distict":"Setiabudi","full_address":"Jl. Jenderal Sudirman No.Kav. 25","phone_number":"(+44)07153419266","postal_code":"12920","region":"Republic of Indonesia","region_code":"ID","state":"Jakarta Province","town":"Karet"},"effect_status":"ENABLED","id":"7354364871201720069","is_default":true,"name":"TikTok Shop Sandbox ID Local Sales warehouse","sub_type":"DOMESTIC_WAREHOUSE","type":"SALES_WAREHOUSE"},{"address":{"city":"South Jakarta City","contact_person":"TikTok Shop Partner Center","distict":"Setiabudi","full_address":"Jl. Jenderal Sudirman No.Kav. 25","phone_number":"(+44)07153419266","postal_code":"12920","region":"Republic of Indonesia","region_code":"ID","state":"Jakarta Province","town":"Karet"},"effect_status":"ENABLED","id":"7354411761147905798","is_default":false,"name":"TikTok Shop Sandbox ID Local Return warehouse","sub_type":"DOMESTIC_WAREHOUSE","type":"RETURN_WAREHOUSE"}]}
     * message : Success
     * request_id : 20240415013359CCDB2CAAF25A09386C81
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
