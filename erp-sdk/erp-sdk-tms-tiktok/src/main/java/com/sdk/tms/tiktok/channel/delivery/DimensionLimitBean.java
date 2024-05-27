package com.sdk.tms.tiktok.channel.delivery;

import com.google.gson.annotations.SerializedName;

public class DimensionLimitBean {
    /**
     * max_height : 160
     * max_length : 160
     * max_width : 160
     * unit : CM
     */

    @SerializedName("max_height")
    private int maxHeight;
    @SerializedName("max_length")
    private int maxLength;
    @SerializedName("max_width")
    private int maxWidth;
    @SerializedName("unit")
    private String unit;

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
