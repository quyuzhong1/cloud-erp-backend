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
            "/webhook/tiktok;"+
            "/fs/callback/api;/thirdProcessDefinition/getFsAppId;"+
            "/sso/login;webhook/kuaidi100/push;"
            ;

    /**
     * token 可选路径：不带 token 也放行；带合法 token 时正常解析并注入登录用户，
     * 以便业务层（如 PDA 版本「跳过此版本」）在已登录场景下仍能拿到真实用户。
     */
    public static final String OPTIONAL_AUTH_PATH_LIST = "/pdaVersion/getPdaVersion;";

    public static final String EVENT_TRACKING_PATH = "/sysEventTracking/add";
}
