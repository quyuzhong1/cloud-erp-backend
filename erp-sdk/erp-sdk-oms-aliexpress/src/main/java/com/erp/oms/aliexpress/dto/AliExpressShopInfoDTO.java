package com.erp.oms.aliexpress.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 平台AliExpress 店铺 DTO
 *
 * @Author yl
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AliExpressShopInfoDTO {

    /**
     * 店铺ID
     */
    private String id;

    /**
     * 访问token
     */
    private String token;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * url
     */
    private String baseUrl;

    /**
     * clientId
     */
    private String clientId;

    /**
     * clientSecret
     */
    private String clientSecret;








}
