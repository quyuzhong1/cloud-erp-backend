package com.sdk.wx.miniapp.constants;

public class WxConstants {
    private WxConstants() {
        throw new IllegalStateException("Utility WxConstants class");
    }

    /**
     * base url
     */
    public static final String BASE_URL = "https://api.weixin.qq.com/";
    /**
     * 获取接口调用凭据
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getAccessToken.html
     *
     */
    public static final String GET_ACCESS_TOKEN = "cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    /**
     * 获取稳定版接口调用凭据
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getStableAccessToken.html
     *
     */
    public static final String GET_STABLE_ACCESS_TOKEN = "cgi-bin/stable_token?grant_type=client_credential&appid=%s&secret=%s&force_refresh=false";
    /**
     * 小程序登录
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
     *
     */
    public static final String JSCODE_TO_SESSION = "sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code ";

    /**
     * 发送订阅消息
     *  https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html
     */
    public static final String SEND_SUBSCRIBE_MESSAGE = "cgi-bin/message/subscribe/send?access_token=%s";

}