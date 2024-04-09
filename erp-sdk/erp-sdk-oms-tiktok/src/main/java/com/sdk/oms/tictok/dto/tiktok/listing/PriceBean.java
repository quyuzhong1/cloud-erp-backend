package com.sdk.oms.tictok.dto.tiktok.listing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceBean {
    /**
     * currency : IDR
     * tax_exclusive_price : 11111111
     */

    @JsonProperty("currency")
    private String currency;
    @JsonProperty("tax_exclusive_price")
    private String taxExclusivePrice;

}
