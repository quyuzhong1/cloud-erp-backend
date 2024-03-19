package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ShippingBean {
    /**
     * mode : not_specified
     * methods : []
     * tags : []
     * dimensions : null
     * local_pick_up : false
     * free_shipping : false
     * logistic_type : not_specified
     * store_pick_up : false
     */

    @JsonProperty("mode")
    private String mode;
    @JsonProperty("dimensions")
    private Object dimensions;
    @JsonProperty("local_pick_up")
    private boolean localPickUp;
    @JsonProperty("free_shipping")
    private boolean freeShipping;
    @JsonProperty("logistic_type")
    private String logisticType;
    @JsonProperty("store_pick_up")
    private boolean storePickUp;
    @JsonProperty("methods")
    private List<?> methods;
    @JsonProperty("tags")
    private List<?> tags;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Object getDimensions() {
        return dimensions;
    }

    public void setDimensions(Object dimensions) {
        this.dimensions = dimensions;
    }

    public boolean isLocalPickUp() {
        return localPickUp;
    }

    public void setLocalPickUp(boolean localPickUp) {
        this.localPickUp = localPickUp;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public void setFreeShipping(boolean freeShipping) {
        this.freeShipping = freeShipping;
    }

    public String getLogisticType() {
        return logisticType;
    }

    public void setLogisticType(String logisticType) {
        this.logisticType = logisticType;
    }

    public boolean isStorePickUp() {
        return storePickUp;
    }

    public void setStorePickUp(boolean storePickUp) {
        this.storePickUp = storePickUp;
    }

    public List<?> getMethods() {
        return methods;
    }

    public void setMethods(List<?> methods) {
        this.methods = methods;
    }

    public List<?> getTags() {
        return tags;
    }

    public void setTags(List<?> tags) {
        this.tags = tags;
    }
}
