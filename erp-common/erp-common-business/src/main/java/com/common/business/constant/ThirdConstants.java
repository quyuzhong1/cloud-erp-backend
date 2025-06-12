package com.common.business.constant;

/**
 * @Classname 第三方常量

 * @Date 2022-07-20 18:09
 * @Created by yl
 */
public class ThirdConstants {

    public static final String FS_GRANT_TYPE = "authorization_code";

    public static final String FS_MESSAGE_TEXT = "text";
    public static final String FS_MESSAGE_INTERACTIVE = "interactive";


    public static final String FS_TOKEN_URL = "https://passport.feishu.cn/suite/passport/oauth/token";

    public static final String FS_USER_URL = "https://passport.feishu.cn/suite/passport/oauth/userinfo";

    //自建应用获取 tenant_access_token
    public static final String FS_TENANT_ACCESS_TOKEN = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

    public static final String FS_USER_ACCESS_TOKEN = "https://open.feishu.cn/open-apis/authen/v1/access_token";

    public static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    //飞书
    public static final String FS_PLATFORM = "FS";

    //钉钉
    public static final String DD_PLATFORM = "DD";


    public static final String THIRD_BINDING_TYPE = "binding";


    public static final String THIRD_LOGIN_TYPE = "login";

    //批量发送消息地址
    public static final String FS_BATCH_SEND_MESSAGE_URL = "https://open.feishu.cn/open-apis/message/v4/batch_send/";


    /**
     * 飞书加急API
     */
    public static final String LARK_PRESS_URL = "https://open.feishu.cn/open-apis/im/v1/messages/{}/urgent_app?user_id_type=union_id";

    /**
     * 飞书发送单条API
     */
    public static final String LARK_SEND_MESSAGE_URL = "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=union_id";

    //发送审批 Bot 消息地址
    public static final String FS_APPROVE_MESSAGE_SEND_URL = "https://open.feishu.cn/open-apis/approval/v1/message/send/";
    //更新审批 Bot 消息地址
    public static final String FS_APPROVE_MESSAGE_UPDATE_URL = "https://open.feishu.cn/open-apis/approval/v1/message/update/";

    public static final String DETAIL_LIST = "detailList";

    public static final String PURCHASE_ORDER_DETAIL = "purchase_order_detail";

    public static final String PURCHASE_ORDER_SUPPLIER = "purchase_order_supplier";

    public static final String CfgProcess = "cfgProcess";

    public static final String CfgThirdProcess = "cfgThirdProcess";
}
