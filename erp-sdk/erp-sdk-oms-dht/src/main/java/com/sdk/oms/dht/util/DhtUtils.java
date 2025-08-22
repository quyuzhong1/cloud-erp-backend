package com.sdk.oms.dht.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sdk.oms.dht.dto.DhtBaseResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DhtUtils {

    public static final String SUCCESS = "Success";


    /**
     * 解析 JSON 字符串，返回 JiFengBaseResp<T>
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型（如 String.class 或自定义 DTO）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> DhtBaseResp<T> parseToJiFengResp(String jsonStr, Class<T> clazz) {
        try {
            return JSON.parseObject(jsonStr, new TypeReference<DhtBaseResp<T>>(clazz) {});
        } catch (Exception e) {
            log.error("订货通 JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return DhtBaseResp.error("JSON 解析失败,原始值：{}，异常: {}", jsonStr,e);
        }
    }

    /**
     * 解析 JSON 字符串（带泛型 TypeReference）
     * @param jsonStr JSON 字符串
     * @param typeRef 目标类型（如 new TypeReference<JiFengBaseResp<String>>() {}）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> DhtBaseResp<T> parseToJiFengResp(String jsonStr, TypeReference<DhtBaseResp<T>> typeRef) {
        try {
            return JSON.parseObject(jsonStr, typeRef);
        } catch (Exception e) {
            log.error("订货通 JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return DhtBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr,e);
        }
    }

}
