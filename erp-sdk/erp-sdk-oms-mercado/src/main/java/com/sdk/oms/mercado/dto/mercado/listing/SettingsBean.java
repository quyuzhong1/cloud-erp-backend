package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SettingsBean {
    /**
     * listing_strategy : open
     */

    @SerializedName("listing_strategy")
    private String listingStrategy;

}
