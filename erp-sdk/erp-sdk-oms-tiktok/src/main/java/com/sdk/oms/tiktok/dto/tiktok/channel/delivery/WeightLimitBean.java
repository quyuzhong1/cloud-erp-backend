package com.sdk.oms.tiktok.dto.tiktok.channel.delivery;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class WeightLimitBean {
    /**
     * max_weight : 100000
     * min_weight : 0
     * unit : GRAM
     */

    @SerializedName("max_weight")
    private int maxWeight;
    @SerializedName("min_weight")
    private int minWeight;
    @SerializedName("unit")
    private String unit;

    public int getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    public int getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(int minWeight) {
        this.minWeight = minWeight;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
