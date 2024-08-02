package com.sdk.oms.tiktok.dto.tiktok.order.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CombinedListingSkusBean {
    /**
     * product_id : 1729582718312380456
     * sku_count : 1
     * sku_id : 2729382476852921123
     */

    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("sku_count")
    private int skuCount;
    @JsonProperty("sku_id")
    private String skuId;

}
