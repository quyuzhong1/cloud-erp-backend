package com.sdk.oms.tictok.dto.tiktok.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InventoryBean {
    /**
     * quantity : 11111
     * warehouse_id : 7354364871201720069
     */

    @JsonProperty("quantity")
    private int quantity;
    @JsonProperty("warehouse_id")
    private String warehouseId;

}
