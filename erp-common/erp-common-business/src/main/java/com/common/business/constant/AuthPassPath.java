package com.common.business.constant;

/**
 * @Classname AuthPassPath

 * @Date 2022-07-14 17:26
 * @Created by yl
 */
public class AuthPassPath {

    private AuthPassPath() {
    }

    public static final String PASS_PATH_LIST = "/user/accountLogin;/user/scanCodeLogin;/calendar/save/year;/user/forgotPasswordGetCode;" +
            "/user/forgotPassword;/shop/shopAuthorize;/shop/shopifyAuthorizeIndex;/shop/shopifyUrl;/shopifyWebhook/customersDataRequest;" +
            "/shopifyWebhook/customersRedact;/shopifyWebhook/shopRedact;" +
            "/user/srmAccountLogin;/user/srmForgotPassword;/user/srmForgotPasswordGetCode;/shop/shopifyUrl;/webVersion/update;/webVersion/sse;"+
            "/open/api/;/logisticsTrack/webhookByTrack123;/webhook/receive/;/overseasInventory/shopifyShippedInfo;"+
            "/fs/callback/api;/thirdProcessDefinition/getFsAppId;"
            ;


    public static final String EVENT_TRACKING_PATH = "/sysEventTracking/add";
}
