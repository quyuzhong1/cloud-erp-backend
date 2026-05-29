package com.sdk.wms.tongyou.utils;

import cn.hutool.core.text.CharSequenceUtil;
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

    /**
     * 解析 JSON 字符串，返回 TongYouBaseResp<T>
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型（如 String.class 或自定义 DTO）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> TongYouBaseResp<T> parseToTongYouResp(String jsonStr, Class<T> clazz) {
        try {
            if (CharSequenceUtil.isBlank(jsonStr)) {
                return TongYouBaseResp.error("通邮接口返回为空");
            }
            TongYouBaseResp<T> resp = JSON.parseObject(jsonStr, new TypeReference<TongYouBaseResp<T>>(clazz) {});
            return resp == null ? TongYouBaseResp.error("通邮接口返回为空") : resp;
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
    public static <T> TongYouBaseResp<T> parseToTongYouResp(String jsonStr, TypeReference<TongYouBaseResp<T>> typeRef) {
        try {
            if (CharSequenceUtil.isBlank(jsonStr)) {
                return TongYouBaseResp.error("通邮接口返回为空");
            }
            TongYouBaseResp<T> resp = JSON.parseObject(jsonStr, typeRef);
            return resp == null ? TongYouBaseResp.error("通邮接口返回为空") : resp;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return TongYouBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr,e);
        }
    }

}
