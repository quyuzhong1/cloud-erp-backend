package com.sdk.oms.tiktok.dto.tiktok.channel.delivery;

import com.google.gson.annotations.SerializedName;

public class DeliveryOptionsBean {
    /**
     * description : only for testflag package with the region ID
     * dimension_limit : {"max_height":160,"max_length":160,"max_width":160,"unit":"CM"}
     * id : 6956553057215710977
     * name : ecom_logistics_type_Standard
     * type : STANDARD
     * weight_limit : {"max_weight":100000,"min_weight":0,"unit":"GRAM"}
     */

    @SerializedName("description")
    private String description;
    @SerializedName("dimension_limit")
    private DimensionLimitBean dimensionLimit;
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("type")
    private String type;
    @SerializedName("weight_limit")
    private WeightLimitBean weightLimit;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DimensionLimitBean getDimensionLimit() {
        return dimensionLimit;
    }

    public void setDimensionLimit(DimensionLimitBean dimensionLimit) {
        this.dimensionLimit = dimensionLimit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WeightLimitBean getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(WeightLimitBean weightLimit) {
        this.weightLimit = weightLimit;
    }
}
