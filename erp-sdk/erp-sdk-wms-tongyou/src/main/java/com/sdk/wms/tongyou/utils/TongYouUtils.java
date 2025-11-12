package com.sdk.wms.tongyou.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TongYouUtils {
    private TongYouUtils() {
        throw new IllegalStateException("Utility TongYouUtils class");
    }
    public static String callService(String service, Object obj){
        String appToken = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appToken"));
        String appKey = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appKey"));
        if(StringUtil.isBlank(appKey) || StringUtil.isBlank(appToken)){
            throw new ServiceException("获取不到授权值，正确授权值为：appToken,appKey");
        }
        String param = JSON.toJSONString(obj);
        ThirdWarehouseContext.setRequestJson(param);
        return "response";
    }

    /**
     * 解析 JSON 字符串，返回 TongYouBaseResp<T>
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型（如 String.class 或自定义 DTO）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> TongYouBaseResp<T> parseToImlResp(String jsonStr, Class<T> clazz) {
        try {
            return JSON.parseObject(jsonStr, new TypeReference<TongYouBaseResp<T>>(clazz) {});
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return TongYouBaseResp.error("JSON 解析失败,原始值：{}，异常: {}", jsonStr,e);
        }
    }

    /**
     * 解析 JSON 字符串（带泛型 TypeReference）
     * @param jsonStr JSON 字符串
     * @param typeRef 目标类型（如 new TypeReference<TongYouBaseResp<String>>() {}）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> TongYouBaseResp<T> parseToImlResp(String jsonStr, TypeReference<TongYouBaseResp<T>> typeRef) {
        try {
            return JSON.parseObject(jsonStr, typeRef);
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return TongYouBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr,e);
        }
    }

}
