package com.sdk.oms.tiktok.dto.tiktok.listing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkusBean {
    /**
     * id : 1729591423391532777
     * inventory : [{"quantity":11111,"warehouse_id":"7354364871201720069"}]
     * price : {"currency":"IDR","tax_exclusive_price":"11111111"}
     * seller_sku : 1111
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("price")
    private PriceBean price;
    @JsonProperty("seller_sku")
    private String sellerSku;
    @JsonProperty("inventory")
    private List<InventoryBean> inventory;

}
