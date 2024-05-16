package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemBean {
    /**
     * id : MLM2802963514
     * title : Ulanzi R099 Kit De Montaje Con Clip Para Cámara Gopro
     * category_id : MLM127873
     * variation_id : null
     * seller_custom_field : null
     * variation_attributes : []
     * warranty : Garantía del vendedor: 1 meses
     * condition : new
     * seller_sku : 2993+1764A+0605
     * parent_item_id : CBT1908713864
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("title")
    private String title;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("variation_id")
    private Object variationId;
    @JsonProperty("seller_custom_field")
    private Object sellerCustomField;
    @JsonProperty("warranty")
    private String warranty;
    @JsonProperty("condition")
    private String condition;
    @JsonProperty("seller_sku")
    private String sellerSku;
    @JsonProperty("parent_item_id")
    private String parentItemId;
    @JsonProperty("variation_attributes")
    private List<?> variationAttributes;

}
