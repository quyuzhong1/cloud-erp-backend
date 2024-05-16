package com.sdk.oms.tiktok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryOptionsBean {
    /**
     * id : 1729592969712203232
     * is_available : true
     * name : ""
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("is_available")
    private boolean isAvailable;
    @JsonProperty("name")
    private String name;

}
