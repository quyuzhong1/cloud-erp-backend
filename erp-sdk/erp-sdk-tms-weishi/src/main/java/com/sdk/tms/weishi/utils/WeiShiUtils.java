package com.sdk.tms.weishi.utils;

import com.common.business.constant.ThirdConstants;
import com.common.core.utils.OkHttpUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeiShiUtils {

    private WeiShiUtils() {
    }

    public static String sendPost(String url, String method, String paramsJson, String appToken, String appKey){
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("paramsJson",paramsJson);
        paramsMap.put("appToken", appToken);
        paramsMap.put("service", method);
        paramsMap.put("appKey", appKey);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", ThirdConstants.CONTENT_TYPE);
        return OkHttpUtils.doPostJson(url, paramsMap, headerMap);
    }

    public static <T> String sendPost(String url, String method, List<T> paramsJson, String appToken, String appKey){
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("paramsJson",paramsJson);
        paramsMap.put("appToken", appToken);
        paramsMap.put("service", method);
        paramsMap.put("appKey", appKey);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", ThirdConstants.CONTENT_TYPE);
        return OkHttpUtils.doPostJson(url, paramsMap, headerMap);
    }
}
