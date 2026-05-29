package com.sdk.wms.damai.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import cn.hutool.core.text.CharSequenceUtil;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiPageBaseResp;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DaMaiUtils {

    private static final String EMPTY_RESPONSE_MESSAGE = "大麦接口返回为空";

    private DaMaiUtils() {
        throw new IllegalStateException("Utility DaMaiUtils class");
    }

    /**
     * 解析 JSON 字符串，返回 JiFengBaseResp<T>
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型（如 String.class 或自定义 DTO）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> DaMaiBaseResp<T> parseToResp(String jsonStr, Class<T> clazz) {
        try {
            if (CharSequenceUtil.isBlank(jsonStr)) {
                return DaMaiBaseResp.error(EMPTY_RESPONSE_MESSAGE);
            }
            DaMaiBaseResp<T> resp = JSON.parseObject(jsonStr, new TypeReference<DaMaiBaseResp<T>>(clazz) {});
            return resp == null ? DaMaiBaseResp.error(EMPTY_RESPONSE_MESSAGE) : resp;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return DaMaiBaseResp.error("JSON 解析失败,原始值：{}，异常: {}", jsonStr,e);
        }
    }

    /**
     * 解析 JSON 字符串（带泛型 TypeReference）
     * @param jsonStr JSON 字符串
     * @param typeRef 目标类型（如 new TypeReference<JiFengBaseResp<String>>() {}）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> DaMaiBaseResp<T> parseToResp(String jsonStr, TypeReference<DaMaiBaseResp<T>> typeRef) {
        try {
            if (CharSequenceUtil.isBlank(jsonStr)) {
                return DaMaiBaseResp.error(EMPTY_RESPONSE_MESSAGE);
            }
            DaMaiBaseResp<T> resp = JSON.parseObject(jsonStr, typeRef);
            return resp == null ? DaMaiBaseResp.error(EMPTY_RESPONSE_MESSAGE) : resp;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return DaMaiBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr,e);
        }
    }


    /**
     * 解析 JSON 字符串，返回 JiFengBaseResp<T>
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型（如 String.class 或自定义 DTO）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> DaMaiPageBaseResp<T> parsePageToResp(String jsonStr, Class<T> clazz) {
        try {
            if (CharSequenceUtil.isBlank(jsonStr)) {
                return DaMaiPageBaseResp.error(EMPTY_RESPONSE_MESSAGE);
            }
            DaMaiPageBaseResp<T> resp = JSON.parseObject(jsonStr, new TypeReference<DaMaiPageBaseResp<T>>(clazz) {});
            return resp == null ? DaMaiPageBaseResp.error(EMPTY_RESPONSE_MESSAGE) : resp;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return DaMaiPageBaseResp.error("JSON 解析失败,原始值：{}，异常: {}", jsonStr,e);
        }
    }

    /**
     * 解析 JSON 字符串（带泛型 TypeReference）
     * @param jsonStr JSON 字符串
     * @param typeRef 目标类型（如 new TypeReference<JiFengBaseResp<String>>() {}）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> DaMaiPageBaseResp<T> parsePageToResp(String jsonStr, TypeReference<DaMaiPageBaseResp<T>> typeRef) {
        try {
            if (CharSequenceUtil.isBlank(jsonStr)) {
                return DaMaiPageBaseResp.error(EMPTY_RESPONSE_MESSAGE);
            }
            DaMaiPageBaseResp<T> resp = JSON.parseObject(jsonStr, typeRef);
            return resp == null ? DaMaiPageBaseResp.error(EMPTY_RESPONSE_MESSAGE) : resp;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return DaMaiPageBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr,e);
        }
    }
}
