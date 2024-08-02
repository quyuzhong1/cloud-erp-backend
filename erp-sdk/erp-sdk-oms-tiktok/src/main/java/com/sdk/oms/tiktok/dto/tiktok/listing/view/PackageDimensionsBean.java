package com.sdk.oms.tiktok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageDimensionsBean {
    /**
     * height : 10
     * length : 10
     * unit : CENTIMETER
     * width : 10
     */

    @SerializedName("height")
    private String height;
    @SerializedName("length")
    private String length;
    @SerializedName("unit")
    private String unit;
    @SerializedName("width")
    private String width;

}
