package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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
