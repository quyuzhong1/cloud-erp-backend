package com.sdk.tms.yanwen.utils;

import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.ThirdConstants;
import com.common.core.utils.Md5Util;
import com.common.core.utils.OkHttpUtils;
import com.sdk.tms.yanwen.constants.YanWenConstants;

import java.util.HashMap;
import java.util.Map;

public class YanWenUtils {

    public static String getSign(String data ,String method,long timestamp){
        return Md5Util.md5(YanWenConstants.API_TOKEN+ YanWenConstants.USER_ID+data+ YanWenConstants.FORMAT+method+timestamp+ YanWenConstants.VERSION+ YanWenConstants.API_TOKEN);
    }

    public static String sendPost(String method,Map<String, Object> paramsMap){
        long timestamp = System.currentTimeMillis();;
        String sign = getSign(JSONObject.toJSONString(paramsMap),method,timestamp);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", ThirdConstants.CONTENT_TYPE);
        String url = YanWenConstants.BASE_URL +
                YanWenConstants.BASE_URL_SUFFIX +
                "?user_id=" +
                YanWenConstants.USER_ID +
                "&method=" +
                method +
                "&format=" +
                YanWenConstants.FORMAT +
                "&timestamp=" +
                timestamp +
                "&sign=" +
                sign +
                "&version=" +
                YanWenConstants.VERSION;
        return OkHttpUtils.doPostJson(url, paramsMap, headerMap);
    }

    public static void main(String[] args) {

    }

}
