package com.sdk.oms.tictok.dto.tiktok.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemTaxBean {
    /**
     * tax_amount : 21.2
     * tax_rate : 0.35
     * tax_type : SALES_TAX
     */

    @JsonProperty("tax_amount")
    private String taxAmount;
    @JsonProperty("tax_rate")
    private String taxRate;
    @JsonProperty("tax_type")
    private String taxType;
}
