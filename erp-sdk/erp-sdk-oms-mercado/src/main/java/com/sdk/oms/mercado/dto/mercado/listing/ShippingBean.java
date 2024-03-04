package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

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

    @SerializedName("mode")
    private String mode;
    @SerializedName("dimensions")
    private Object dimensions;
    @SerializedName("local_pick_up")
    private boolean localPickUp;
    @SerializedName("free_shipping")
    private boolean freeShipping;
    @SerializedName("logistic_type")
    private String logisticType;
    @SerializedName("store_pick_up")
    private boolean storePickUp;
    @SerializedName("methods")
    private List<?> methods;
    @SerializedName("tags")
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
