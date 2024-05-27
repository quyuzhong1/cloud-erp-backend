package com.sdk.oms.tiktok.dto.tiktok.shop;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class DataBean {
    @SerializedName("shops")
    private List<ShopsBean> shops;

}
