package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SellerAddressBean {


    /**
     * seller_address : {"city":{"id":"SEstSEtLd2FpIFRzaW5n","name":"Kwai Tsing"},"state":{"id":"HK-HK","name":"Hong Kong"},"country":{"id":"HK","name":"Hong Kong"},"id":1332219696}
     */

    @JsonProperty("seller_address")
    private SellerAddressBeanX sellerAddress;

    public SellerAddressBeanX getSellerAddress() {
        return sellerAddress;
    }

    public void setSellerAddress(SellerAddressBeanX sellerAddress) {
        this.sellerAddress = sellerAddress;
    }
}
