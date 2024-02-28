package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

public class SettingsBean {
    /**
     * listing_strategy : open
     */

    @SerializedName("listing_strategy")
    private String listingStrategy;

    public String getListingStrategy() {
        return listingStrategy;
    }

    public void setListingStrategy(String listingStrategy) {
        this.listingStrategy = listingStrategy;
    }
}
