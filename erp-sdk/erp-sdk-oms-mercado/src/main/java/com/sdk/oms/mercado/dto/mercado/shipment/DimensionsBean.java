package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;

public class DimensionsBean {
    /**
     * height : 6
     * length : 11
     * weight : 190
     * width : 5
     */

    @SerializedName("height")
    private int height;
    @SerializedName("length")
    private int length;
    @SerializedName("weight")
    private int weight;
    @SerializedName("width")
    private int width;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
