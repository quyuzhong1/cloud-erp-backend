package com.sdk.oms.tiktok.dto.tiktok.split;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class SplitViewBean {
    @SerializedName("packages")
    private List<PackagesBean> packages;
}
