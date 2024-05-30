package com.sdk.oms.tiktok.dto.tiktok.order.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryPreferencesBean {
    /**
     * drop_off_location : Front Door
     */

    @JsonProperty("drop_off_location")
    private String dropOffLocation;

}
