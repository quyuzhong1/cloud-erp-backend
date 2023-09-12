package com.sdk.oms.shopify.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台Shopify 店铺 DTO
 *
 * @Author Jim
 **/
@Data
@NoArgsConstructor
public class ShopifyShopInfoDTO {

    /**
     * 店铺域名
     */
    private String shopDomain;

    /**
     * 访问token
     */
    private String accessToken;

}
