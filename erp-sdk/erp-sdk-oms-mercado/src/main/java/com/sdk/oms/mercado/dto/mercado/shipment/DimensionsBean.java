package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DimensionsBean {
    /**
     * height : 6
     * length : 11
     * weight : 190
     * width : 5
     */

    @JsonProperty("height")
    private int height;
    @JsonProperty("length")
    private int length;
    @JsonProperty("weight")
    private int weight;
    @JsonProperty("width")
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
