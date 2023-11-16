package com.sdk.wms.goodcang.utils;

import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.goodcang.constants.GoodCangConstants;

import java.util.HashMap;
import java.util.Map;

public class GoodCangUtils {

    public static String sendPost(String apiUrl, Map<String, Object> paramsMap, String appToken, String appKey){
        Map<String,String> headerMap = headerMap("7013991264f611e98ea200e01b680258","6ff50abf64f611e98ea200e01b680258");
//        Map<String,String> headerMap = headerMap("a39ab99c1437c991ec07fad4e1f78f8f","f7e4102f9b0b983e58bed3140dc22f1a");
        String url = GoodCangConstants.BASE_URL + apiUrl;
        return OkHttpUtils.doPostJson(url, paramsMap, headerMap);
    }

    public static String sendPost(String apiUrl, String paramsJson, String appToken, String appKey){
        Map<String,String> headerMap = headerMap("7013991264f611e98ea200e01b680258","6ff50abf64f611e98ea200e01b680258");
//        Map<String,String> headerMap = headerMap("a39ab99c1437c991ec07fad4e1f78f8f","f7e4102f9b0b983e58bed3140dc22f1a");
        String url = GoodCangConstants.BASE_URL + apiUrl;
        return OkHttpUtils.doPostJson(url, paramsJson, headerMap);
    }

    public static Map<String,String> headerMap(String appToken,String appKey){
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("app-token", appToken);
        headerMap.put("app-key" ,appKey);
        return headerMap;
    }

}
