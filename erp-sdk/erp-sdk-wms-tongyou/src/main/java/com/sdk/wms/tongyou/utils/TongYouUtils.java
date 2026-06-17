package com.sdk.wms.tongyou.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.exception.ThirdWarehouseEmptyResponseException;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TongYouUtils {
    private static final String EMPTY_RESPONSE_MESSAGE = "通邮接口返回为空";

    private TongYouUtils() {
        throw new IllegalStateException("Utility TongYouUtils class");
    }

    private static void assertResponseNotBlank(String jsonStr) {
        if (CharSequenceUtil.isBlank(jsonStr)) {
            throw new ThirdWarehouseEmptyResponseException(EMPTY_RESPONSE_MESSAGE);
        }
    }

    /**
     * 解析 JSON 字符串，返回 TongYouBaseResp<T>
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型（如 String.class 或自定义 DTO）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> TongYouBaseResp<T> parseToTongYouResp(String jsonStr, Class<T> clazz) {
        try {
            assertResponseNotBlank(jsonStr);
            TongYouBaseResp<T> resp = JSON.parseObject(jsonStr, new TypeReference<TongYouBaseResp<T>>(clazz) {});
            if (resp == null) {
                throw new ThirdWarehouseEmptyResponseException(EMPTY_RESPONSE_MESSAGE);
            }
            return resp;
        } catch (ThirdWarehouseEmptyResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            // TongYouBaseResp.error(String, Object...) 内部使用 Hutool CharSequenceUtil.format，支持 {} 占位符。
            return TongYouBaseResp.error("JSON 解析失败,原始值：{}，异常: {}", jsonStr,e);
        }
    }

    /**
     * 解析 JSON 字符串（带泛型 TypeReference）
     * @param jsonStr JSON 字符串
     * @param typeRef 目标类型（如 new TypeReference<TongYouBaseResp<String>>() {}）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> TongYouBaseResp<T> parseToTongYouResp(String jsonStr, TypeReference<TongYouBaseResp<T>> typeRef) {
        try {
            assertResponseNotBlank(jsonStr);
            TongYouBaseResp<T> resp = JSON.parseObject(jsonStr, typeRef);
            if (resp == null) {
                throw new ThirdWarehouseEmptyResponseException(EMPTY_RESPONSE_MESSAGE);
            }
            return resp;
        } catch (ThirdWarehouseEmptyResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            // TongYouBaseResp.error(String, Object...) 内部使用 Hutool CharSequenceUtil.format，支持 {} 占位符。
            return TongYouBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr,e);
        }
    }


    /**
     * 解析通邮列表响应：兼容标准包装格式与直接返回 JSON 数组两种结构
     */
    public static <T> TongYouBaseResp<List<T>> parseToTongYouListResp(String jsonStr, Class<T> itemClass,
                                                                      TypeReference<TongYouBaseResp<List<T>>> typeRef) {
        try {
            String trimmed = jsonStr == null ? "" : jsonStr.trim();
            if (trimmed.startsWith("[")) {
                List<T> data = JSON.parseArray(trimmed, itemClass);
                TongYouBaseResp<List<T>> resp = new TongYouBaseResp<>();
                resp.setError("T");
                resp.setData(data);
                return resp;
            }
            return JSON.parseObject(jsonStr, typeRef);
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr, e);
            return TongYouBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr, e);
        }
    }


}
