package com.sdk.tms.yuntu.utils;

import com.common.business.constant.ThirdConstants;
import com.common.core.security.SBase64;
import com.common.core.utils.OkHttpUtils;
import com.sdk.tms.yuntu.constants.YunTuConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YunTuUtils {

    public static String getToken(){
        return SBase64.stringToBase64(YunTuConstants.CUSTOMER_CODE+"&"+YunTuConstants.API_SECRET);
    }

    public static String sendPost(String apiUrl, List<Map<String, Object>> paramsMapList){
        Map<String, String> headerMap = headerMap();
        String url = YunTuConstants.BASE_URL + apiUrl;
        return OkHttpUtils.doPostJson(url, paramsMapList, headerMap);
    }

    public static String sendPost(String apiUrl, Map<String, Object> paramsMap){
        Map<String, String> headerMap = headerMap();
        String url = YunTuConstants.BASE_URL + apiUrl;
        return OkHttpUtils.doPostJson(url, paramsMap, headerMap);
    }

    public static String sendPostList(String apiUrl, List<String> list){
        Map<String, String> headerMap = headerMap();
        String url = YunTuConstants.BASE_URL + apiUrl;
        return OkHttpUtils.doPostList(url, list, headerMap);
    }

    public static String sendGet(String apiUrl, Map<String, Object> paramsMap){
        Map<String, String> headerMap = headerMap();
        String url = YunTuConstants.BASE_URL + apiUrl;
        return OkHttpUtils.doGet(url, paramsMap, headerMap);
    }

    public static Map<String,String> headerMap(){
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", ThirdConstants.CONTENT_TYPE);
        headerMap.put("Authorization","Basic " + getToken());
        return headerMap;
    }

}
