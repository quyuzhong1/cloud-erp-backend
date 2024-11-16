package com.erp.server.msg.constant;

/**
 * @Classname: FeishuConstant

 * @CreateTime: 2023-04-19  10:42
 * @Author: zhangchunlin
 */
public class FeishuConstant {
    private FeishuConstant() {
        throw new IllegalStateException("Utility FeishuConstant class");
    }
    // 飞书权限请求头前缀
    public final static String FS_AUTHORIZATION = "Bearer ";

    // 飞书请求内容类型
    public final static String CONTENT_TYPE = "application/json;charset=UTF-8";

    // 获取token
    public final static String FS_TOKEN_URL = "https://passport.feishu.cn/suite/passport/oauth/token";

    // 获取用户信息
    public final static String FS_USER_URL = "https://passport.feishu.cn/suite/passport/oauth/userinfo";

    // 自建应用获取 tenant_access_token
    public final static String FS_TENANT_ACCESS_TOKEN = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

    /**
     * 飞书发送单条API
     */
    public final static String LARK_SEND_MESSAGE_URL = "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=union_id";

    /**
     * 飞书加急API
     */
    public final static String LARK_PRESS_URL = "https://open.feishu.cn/open-apis/im/v1/messages/{}/urgent_app?user_id_type=union_id";

    /**
     * 批量发送消息地址
     */
    public final static String FS_BATCH_SEND_MESSAGE_URL = "https://open.feishu.cn/open-apis/message/v4/batch_send/";

    /**
     * 飞书预警消息发送URL
     */
    public final static String FS_WARN_HOOK_URL = "https://open.feishu.cn/open-apis/bot/v2/hook/{}";

}
