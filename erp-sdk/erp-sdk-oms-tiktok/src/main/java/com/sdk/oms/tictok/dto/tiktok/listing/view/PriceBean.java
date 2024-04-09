package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceBean {
    /**
     * currency : USD
     * sale_price : 117.5
     * tax_exclusive_price : 110
     */

    @JsonProperty("currency")
    private String currency;
    @JsonProperty("sale_price")
    private String salePrice;
    @JsonProperty("tax_exclusive_price")
    private String taxExclusivePrice;

}
