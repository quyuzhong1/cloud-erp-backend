package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageWeightBean {
    /**
     * unit : KILOGRAM
     * value : 1.32
     */

    @JsonProperty("unit")
    private String unit;
    @JsonProperty("value")
    private String value;

}
