package com.sdk.wms.iml.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.Md5Util;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.soap.Ec;
import com.sdk.wms.iml.soap.Ec_Service;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ImlUtils {

    private String getPreUrl(){
        if (BusinessCommonConstants.hasProfile("prod")) {
            return "https://open.imlb2c.com/";
        } else {
            return "http://pre-open.imlb2c.cn/";
        }
    }

    public static String callService(String service, Object obj){
        Ec_Service ecService = new Ec_Service();
        Ec ec = ecService.getEcSOAP();
        String appToken = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appToken"));
        String appKey = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appKey"));
        if(StringUtil.isBlank(appKey) || StringUtil.isBlank(appToken)){
            throw new ServiceException("获取不到授权值，正确授权值为：appToken,appKey");
        }
        String param = JSON.toJSONString(obj);
        String response =  ec.callService(param,appToken,appKey,service);
        ThirdWarehouseContext.setRequestJson(param);
        ThirdWarehouseContext.setResponseJson(response);
        return response;
    }

    public static Map<String, String> buildHearderMap(Map<String,Object> body) {
        String appId = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appId"));
        String appSecret = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appSecret"));
        String appToken = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appToken"));
        if(StringUtil.isBlank(appToken) || StringUtil.isBlank(appToken)){
            throw new ServiceException("获取不到授权值，正确授权值为：appId,appSecret,appToken");
        }
        String timestamp = String.valueOf(new Date().getTime());
        String appSign = Md5Util.md5(appSecret + timestamp + JSONObject.toJSONString(body));
        Map<String,String> headerMap = new HashMap<>();
        headerMap.put("x-app-id",appId);
        headerMap.put("x-app-sign",appSign);
        headerMap.put("x-request-time",timestamp);
        headerMap.put("x-request-token",appToken);
        return headerMap;
    }

    /**
     * 解析 JSON 字符串，返回 JiFengBaseResp<T>
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型（如 String.class 或自定义 DTO）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> ImlBaseResp<T> parseToImlResp(String jsonStr, Class<T> clazz) {
        try {
            return JSON.parseObject(jsonStr, new TypeReference<ImlBaseResp<T>>(clazz) {});
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return ImlBaseResp.error("JSON 解析失败,原始值：{}，异常: {}", jsonStr,e);
        }
    }

    /**
     * 解析 JSON 字符串（带泛型 TypeReference）
     * @param jsonStr JSON 字符串
     * @param typeRef 目标类型（如 new TypeReference<JiFengBaseResp<String>>() {}）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> ImlBaseResp<T> parseToImlResp(String jsonStr, TypeReference<ImlBaseResp<T>> typeRef) {
        try {
            return JSON.parseObject(jsonStr, typeRef);
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return ImlBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr,e);
        }
    }

}
