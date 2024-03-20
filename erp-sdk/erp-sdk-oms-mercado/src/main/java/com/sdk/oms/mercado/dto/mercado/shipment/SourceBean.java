package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SourceBean {
    /**
     * site_id : MLM
     */

    @JsonProperty("site_id")
    private String siteId;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
