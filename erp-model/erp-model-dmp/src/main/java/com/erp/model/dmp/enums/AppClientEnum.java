package com.erp.model.dmp.enums;

/**
 * @author Lambda
 * @Classname AppClientEnum
 * @Description TODO
 * @Date 2023-08-29 10:35
 * @Created by yl
 */
public enum AppClientEnum  {

    SHOP_AUTHORIZE("shopAuthorize","sales","Shopify"),
    SHOP_ACCESS_TOKEN("shopAccessToken","sales","Shopify"),
    SHOP_AUTHORIZE_INSTALL("shopAuthorizeInstall","sales","Shopify"),


    ;

    /**
     *  业务类型
     */
    private String businessType;

    /**
     * 平台类型
     */
    private String platformType;

    /**
     * 平台
     */
    private String platform;

    public String getBusinessType() {
        return businessType;
    }

    public String getPlatformType() {
        return platformType;
    }

    public String getPlatform() {
        return platform;
    }
    AppClientEnum(String businessType, String platformType,String platform) {
        this.businessType = businessType;
        this.platformType = platformType;
        this.platform = platform;
    }
}
