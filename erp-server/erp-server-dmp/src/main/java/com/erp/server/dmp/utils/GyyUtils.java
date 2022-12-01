package com.erp.server.dmp.utils;

import com.common.core.utils.Md5Util;

public class GyyUtils {
    /**
     * 得到sign的字符串
     * @Author Luo_WG
     * @Date 2022/11/17 14:46
     * @param jsonDate 请求参数
     * @param secret 秘钥
     * @return java.lang.String
     **/
    public static String sign(String jsonDate, String secret) {
        StringBuilder enValue = new StringBuilder();
        enValue.append(secret);
        enValue.append(jsonDate);
        enValue.append(secret);
        return Md5Util.md5(enValue.toString());
    }
}
