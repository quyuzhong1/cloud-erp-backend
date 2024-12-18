package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class ReturnWarehouseAddressBean {
    /**
     * full_address : 1199 Coleman Ave San Jose, CA 95110
     */

    @SerializedName("full_address")
    private String fullAddress;

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
}
