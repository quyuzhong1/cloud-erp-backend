package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DestinationBean {
    /**
     * type : buying_address
     * receiver_id : 139133205
     * receiver_name : Wilbert Geovany Alonzo Graniel
     * receiver_phone : 9847459241
     * comments :
     * shipping_address : {"address_id":271976606,"address_line":"2 Poniente 196","street_name":"2 Poniente","street_number":"196","comment":"M23L7 Referencia: M23L7","zip_code":"77760","city":{"id":"TVgtUk9PVHVsdW0","name":"Tulum"},"state":{"id":"MX-ROO","name":"Quintana Roo"},"country":{"id":"MX","name":"Mexico"},"neighborhood":{"id":null,"name":"Guerra de Castas"},"municipality":{"id":null,"name":"Tulum"},"agency":{"agency_id":null,"carrier_id":null,"description":null,"open_hours":null,"phone":null,"type":null},"types":["billing","default_buying_address","default_return_address","shipping"],"latitude":20.21588,"longitude":-87.455037,"geolocation_type":"ROOFTOP","geolocation_last_updated":"2023-10-09T22:22:49.854Z","geolocation_source":"map-verified","delivery_preference":"business"}
     */

    @JsonProperty("type")
    private String type;
    @JsonProperty("receiver_id")
    private int receiverId;
    @JsonProperty("receiver_name")
    private String receiverName;
    @JsonProperty("receiver_phone")
    private String receiverPhone;
    @JsonProperty("comments")
    private String comments;
    @JsonProperty("shipping_address")
    private ShippingAddressBeanX shippingAddress;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public ShippingAddressBeanX getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddressBeanX shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
