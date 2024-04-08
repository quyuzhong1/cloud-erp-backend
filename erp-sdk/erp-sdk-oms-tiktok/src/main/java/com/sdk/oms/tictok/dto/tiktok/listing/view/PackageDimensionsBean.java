package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PackageDimensionsBean {
    /**
     * height : 10
     * length : 10
     * unit : CENTIMETER
     * width : 10
     */

    @JsonProperty("height")
    private String height;
    @JsonProperty("length")
    private String length;
    @JsonProperty("unit")
    private String unit;
    @JsonProperty("width")
    private String width;
}
