package com.sdk.oms.tictok.dto.tiktok.shop;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ShopsBean {
    @SerializedName("cipher")
    private String cipher;
    @SerializedName("code")
    private String code;
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("region")
    private String region;
    @SerializedName("seller_type")
    private String sellerType;
}
