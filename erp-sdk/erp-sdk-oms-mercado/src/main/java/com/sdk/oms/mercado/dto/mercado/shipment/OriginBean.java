package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;

public class OriginBean {
    /**
     * sender_id : 1511265855
     * shipping_address : {"address_id":1331228739,"address_line":"XXXXXXX","street_name":"XXXXXXX","street_number":"XXXXXXX","comment":null,"zip_code":"XXXXXXX","city":{"id":"SEstSEtLd2FpIFRzaW5n","name":"Kwai Tsing"},"state":{"id":"HK-HK","name":"Hong Kong"},"country":{"id":"HK","name":"Hong Kong"},"neighborhood":{"id":null,"name":null},"municipality":{"id":null,"name":null},"agency":{"agency_id":null,"carrier_id":null,"description":null,"open_hours":null,"phone":null,"type":null},"types":["billing","default_selling_address","shipping"],"latitude":0,"longitude":0,"geolocation_type":null,"geolocation_last_updated":null,"geolocation_source":null,"delivery_preference":""}
     * type : selling_address
     */

    @SerializedName("sender_id")
    private int senderId;
    @SerializedName("shipping_address")
    private ShippingAddressBean shippingAddress;
    @SerializedName("type")
    private String type;

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public ShippingAddressBean getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddressBean shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
