package com.sdk.tms.tongyou.utils;

import com.common.business.constant.ThirdConstants;
import com.common.core.utils.Md5Util;
import com.common.core.utils.OkHttpUtils;
import com.sdk.tms.tongyou.constants.TongYouConstants;
import com.sdk.tms.tongyou.dto.TongYouSignDTO;

import java.util.HashMap;
import java.util.Map;

public class TongYouUtils {

    public static String sendPost(String method,Map<String, Object> paramsMap){
        Map<String, String> headerMap = headerMap();
        return OkHttpUtils.doPostJson(TongYouConstants.BASE_URL + method, paramsMap, headerMap);
    }

    public static String sendPost(String method,Map<String, Object> paramsMap,String sign){
        Map<String, String> headerMap = headerMap();
        headerMap.put("sign",sign);
        return OkHttpUtils.doPostJson(TongYouConstants.BASE_URL + method, paramsMap, headerMap);
    }

    public static Map<String,String> headerMap(){
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", ThirdConstants.CONTENT_TYPE);
        headerMap.put("userToken", TongYouConstants.USER_TOKEN);
        return headerMap;
    }

    public static String getCreateOrderSign(TongYouSignDTO tongYouSignDTO){
        return Md5Util.md5UpperCase(TongYouConstants.USER_TOKEN+tongYouSignDTO.sign());
    }

    public static String getCallBackOrderSign(String logisticsId,String orderNo){
        return Md5Util.md5UpperCase(TongYouConstants.USER_TOKEN+logisticsId+orderNo);
    }

    public static String getPrintLabelSign(String logisticsId,String orderNo,String trackNo){
        return Md5Util.md5UpperCase(TongYouConstants.USER_TOKEN+logisticsId+orderNo + trackNo);
    }

}
