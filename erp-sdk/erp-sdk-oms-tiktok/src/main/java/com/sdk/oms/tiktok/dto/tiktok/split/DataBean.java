package com.sdk.oms.tiktok.dto.tiktok.split;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataBean {
    @SerializedName("split_attributes")
    private List<SplitAttributesBean> splitAttributes;

    public List<SplitAttributesBean> getSplitAttributes() {
        return splitAttributes;
    }

    public void setSplitAttributes(List<SplitAttributesBean> splitAttributes) {
        this.splitAttributes = splitAttributes;
    }
}
