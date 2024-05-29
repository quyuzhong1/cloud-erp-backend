package com.sdk.oms.tiktok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryBean {
    /**
     * quantity : 999
     * warehouse_id : 6966568648651605766
     */

    @JsonProperty("quantity")
    private int quantity;
    @JsonProperty("warehouse_id")
    private String warehouseId;

}
