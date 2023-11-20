package com.sdk.wms.goodcang.utils;

import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.goodcang.constants.GoodCangConstants;

import java.util.HashMap;
import java.util.Map;

public class GoodCangUtils {

    public static String sendPost(String apiUrl, Map<String, Object> paramsMap){
        Map<String,String> headerMap = headerMap();
        String url = GoodCangConstants.BASE_URL + apiUrl;
        String response = OkHttpUtils.doPostJson(url, paramsMap, headerMap);
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(paramsMap));
        ThirdWarehouseContext.setResponseJson(response);
        return response;
    }

    public static String sendPost(String apiUrl, String paramsJson){
        Map<String,String> headerMap = headerMap();
        String url = GoodCangConstants.BASE_URL + apiUrl;
        String response = OkHttpUtils.doPostJson(url, paramsJson, headerMap);
        ThirdWarehouseContext.setRequestJson(paramsJson);
        ThirdWarehouseContext.setResponseJson(response);
        return response;
    }

    public static Map<String,String> headerMap(){
        String appToken = ThirdWarehouseContext.getAuthMap().get("appToken");
        String appKey = ThirdWarehouseContext.getAuthMap().get("appKey");
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("app-token", appToken);
        headerMap.put("app-key" ,appKey);
        return headerMap;
    }

}
