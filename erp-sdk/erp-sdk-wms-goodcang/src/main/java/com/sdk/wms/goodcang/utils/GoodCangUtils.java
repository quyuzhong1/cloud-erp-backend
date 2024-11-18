package com.sdk.wms.goodcang.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GoodCangUtils {
    private GoodCangUtils() {
    }
    @Value("${warehouse.goodcang.url}")
    private static String BASE_URL;

    public static String sendPost(String apiUrl, Map<String, Object> paramsMap){
        Map<String,String> headerMap = headerMap();
        String url = BASE_URL + apiUrl;
        String response = OkHttpUtils.doPostJson(url, paramsMap, headerMap);
        ThirdWarehouseContext.setRequestJson(JSON.toJSONString(paramsMap));
        ThirdWarehouseContext.setResponseJson(response);
        return response;
    }

    public static String sendPost(String apiUrl, String paramsJson){
        Map<String,String> headerMap = headerMap();
        String url = BASE_URL + apiUrl;
        String response = OkHttpUtils.doPostJson(url, paramsJson, headerMap);
        ThirdWarehouseContext.setRequestJson(paramsJson);
        ThirdWarehouseContext.setResponseJson(response);
        return response;
    }

    public static Map<String,String> headerMap(){
        String appToken = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appToken"));
        String appKey = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appKey"));
        if(StringUtil.isBlank(appKey) || StringUtil.isBlank(appToken)){
            throw new ServiceException("获取不到授权值，正确授权值为：appToken,appKey");
        }
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("app-token", appToken);
        headerMap.put("app-key" ,appKey);
        return headerMap;
    }

}
