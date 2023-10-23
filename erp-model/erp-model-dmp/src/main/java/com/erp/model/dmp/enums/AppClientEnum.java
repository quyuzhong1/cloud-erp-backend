package com.erp.model.dmp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Lambda
 * @Classname AppClientEnum
 * @Description TODO
 * @Date 2023-08-29 10:35
 * @Created by yl
 */
@Getter
@AllArgsConstructor
public enum AppClientEnum  {

    SHOP_AUTHORIZE("shopAuthorize","sales","Shopify"),
    SHOP_ACCESS_TOKEN("shopAccessToken","sales","Shopify"),
    SHOP_AUTHORIZE_INSTALL("shopAuthorizeInstall","sales","Shopify"),
    WALMART_AUTHORIZE("walmartAuthorize","sales","walmart"),
    WALMART_ACCESS_TOKEN("walmartAccessToken","sales","walmart"),
    SHOPEE_ACCESS_TOKEN("shopAccessToken","sales","shopee"),


    ;

    /**
     *  业务类型
     */
    private final String businessType;

    /**
     * 平台类型
     */
    private final String platformType;

    /**
     * 平台
     */
    private final String platform;

}
