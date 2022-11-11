package com.common.core.constant;

/**
 * @Classname 第三方常量
 * @Description TODO
 * @Date 2022-07-20 18:09
 * @Created by yl
 */
public interface ThirdConstants {

    String FS_GRANT_TYPE="authorization_code";


    String FS_TOKEN_URL="https://passport.feishu.cn/suite/passport/oauth/token";

    String FS_USER_URL="https://passport.feishu.cn/suite/passport/oauth/userinfo";

    //自建应用获取 tenant_access_token
    String FS_TENANT_ACCESS_TOKEN="https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

    String FS_USER_ACCESS_TOKEN="https://open.feishu.cn/open-apis/authen/v1/access_token";

    String CONTENT_TYPE = "application/json;charset=UTF-8";

    //飞书
    String FS_PLATFORM="FS";

    //钉钉
    String DD_PLATFORM="DD";


    String THIRD_BINDING_TYPE="binding";


    String THIRD_LOGIN_TYPE="login";

}
