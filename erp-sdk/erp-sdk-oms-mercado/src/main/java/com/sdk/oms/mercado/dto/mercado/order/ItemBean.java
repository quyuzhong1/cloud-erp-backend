package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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
    private String id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Object getVariationId() {
        return variationId;
    }

    public void setVariationId(Object variationId) {
        this.variationId = variationId;
    }

    public Object getSellerCustomField() {
        return sellerCustomField;
    }

    public void setSellerCustomField(Object sellerCustomField) {
        this.sellerCustomField = sellerCustomField;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public String getParentItemId() {
        return parentItemId;
    }

    public void setParentItemId(String parentItemId) {
        this.parentItemId = parentItemId;
    }

    public List<?> getVariationAttributes() {
        return variationAttributes;
    }

    public void setVariationAttributes(List<?> variationAttributes) {
        this.variationAttributes = variationAttributes;
    }
}
