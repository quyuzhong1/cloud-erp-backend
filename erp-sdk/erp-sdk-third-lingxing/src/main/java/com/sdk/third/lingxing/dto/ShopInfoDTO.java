package com.sdk.third.lingxing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领星店铺响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopInfoDTO {
    @JsonProperty("sid")
    private int sid;

    @JsonProperty("mid")
    private int merchantId;

    @JsonProperty("name")
    private String shopName;

    @JsonProperty("seller_id")
    private String sellerId;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("seller_account_id")
    private int sellerAccountId;

    @JsonProperty("region")
    private String region;

    @JsonProperty("country")
    private String country;

    /**
     * 是否授权广告：
     * 0 否
     * 1 是
     */
    @JsonProperty("has_ads_setting")
    private int hasAdsSetting;

    @JsonProperty("marketplace_id")
    private String marketplaceId;

    /**
     * 店铺状态：
     * 0 停止同步
     * 1 正常
     * 2 授权异常
     * 3 欠费停服
     */
    @JsonProperty("status")
    private int status;

}
