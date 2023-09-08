package com.sdk.oms.shopify.dto;

import com.common.business.dto.CleanBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台Shopify token DTO
 *
 * @Author Jim
 **/
@Data
@NoArgsConstructor
public class ShopifyTokenDTO {

    /**
     * 店铺域名
     */
    private String shopDomain;

    /**
     * 访问token
     */
    private String accessToken;

}
