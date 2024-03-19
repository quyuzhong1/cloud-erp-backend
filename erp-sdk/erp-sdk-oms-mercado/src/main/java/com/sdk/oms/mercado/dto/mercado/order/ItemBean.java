package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
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

    @SerializedName("id")
    private String fid;
    @SerializedName("title")
    private String title;
    @SerializedName("category_id")
    private String categoryId;
    @SerializedName("variation_id")
    private Object variationId;
    @SerializedName("seller_custom_field")
    private Object sellerCustomField;
    @SerializedName("warranty")
    private String warranty;
    @SerializedName("condition")
    private String condition;
    @SerializedName("seller_sku")
    private String sellerSku;
    @SerializedName("parent_item_id")
    private String parentItemId;
    @SerializedName("variation_attributes")
    private List<?> variationAttributes;

}
