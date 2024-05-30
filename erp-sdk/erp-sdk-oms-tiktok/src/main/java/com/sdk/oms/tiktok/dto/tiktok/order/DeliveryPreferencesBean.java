package com.sdk.oms.tiktok.dto.tiktok.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryPreferencesBean {
    /**
     * drop_off_location : Front Door
     */

    @SerializedName("drop_off_location")
    private String dropOffLocation;

}
