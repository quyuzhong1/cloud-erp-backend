package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemBean {
    /**
     * id : MLB5283694678
     * title : Ulanzi J12 Lavalier Microfone Sem Fio Para iPhone iPad
     * category_id : MLB439409
     * variation_id : 186902719495
     * seller_custom_field : null
     * global_price : null
     * net_weight : null
     * variation_attributes : [{"name":"Cor","id":"COLOR","value_id":"52049","value_name":"Preto"}]
     * warranty : Garantia de fábrica: 90 dias
     * condition : new
     * seller_sku : 2885
     */

    @JsonProperty("id")
    private String id;
    @JsonProperty("title")
    private String title;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("variation_id")
    private long variationId;
    @JsonProperty("seller_custom_field")
    private Object sellerCustomField;
    @JsonProperty("global_price")
    private Object globalPrice;
    @JsonProperty("net_weight")
    private Object netWeight;
    @JsonProperty("warranty")
    private String warranty;
    @JsonProperty("condition")
    private String condition;
    @JsonProperty("seller_sku")
    private String sellerSku;
    @JsonProperty("variation_attributes")
    private List<VariationAttributesBean> variationAttributes;

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

    public long getVariationId() {
        return variationId;
    }

    public void setVariationId(long variationId) {
        this.variationId = variationId;
    }

    public Object getSellerCustomField() {
        return sellerCustomField;
    }

    public void setSellerCustomField(Object sellerCustomField) {
        this.sellerCustomField = sellerCustomField;
    }

    public Object getGlobalPrice() {
        return globalPrice;
    }

    public void setGlobalPrice(Object globalPrice) {
        this.globalPrice = globalPrice;
    }

    public Object getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Object netWeight) {
        this.netWeight = netWeight;
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

    public List<VariationAttributesBean> getVariationAttributes() {
        return variationAttributes;
    }

    public void setVariationAttributes(List<VariationAttributesBean> variationAttributes) {
        this.variationAttributes = variationAttributes;
    }
}
