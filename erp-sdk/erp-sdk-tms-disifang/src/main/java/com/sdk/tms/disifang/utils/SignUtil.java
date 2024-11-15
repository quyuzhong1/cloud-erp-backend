package com.sdk.tms.disifang.utils;

import com.sdk.tms.disifang.model.base.AffterentParam;

/**
 * @author zdy
 * @ClassName SignUtil

 * @date 2023年11月02日
 * @version: 1.0
 */
public class SignUtil {

    private SignUtil(){}

    public static String getSign(String appkey, String format, String method, String timestamp, String version, String bodyJson, String appSecret) {
        String signCause = String.format("app_key%sformat%smethod%stimestamp%sv%s%s%s", new Object[]{appkey, format, method, timestamp, version, bodyJson, appSecret});
        String sign = MD5Util.doMd5(signCause).toLowerCase();
        return sign;
    }

    public static String getSingByParam(AffterentParam param, String bodyJson, Long timestamp) {
        String sign = getSign(param.getAppKey(), param.getFormat(), param.getMethod(), timestamp.toString(), param
                .getVersion(), bodyJson, param.getAppSecret());
        return sign;
    }
}
