package com.sdk.wms.jifeng.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.exception.ThirdWarehouseEmptyResponseException;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JiFengUtils {

    public static final String SUCCESS = "Success";
    private static final String EMPTY_RESPONSE_MESSAGE = "极风接口返回为空";

    private static void assertResponseNotBlank(String jsonStr) {
        if (CharSequenceUtil.isBlank(jsonStr)) {
            throw new ThirdWarehouseEmptyResponseException(EMPTY_RESPONSE_MESSAGE);
        }
    }

    public static String sign(String key, String data) {
        return hmacsha256(key, data);
    }
    public static String sign(String key, Map<String, String> params) {
        String data = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).sorted().collect(Collectors.joining("&"));
        return sign(key, data);
    }
    public static String hmacsha256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            hmac.init(secret_key);
            return new String(Hex.encodeHex(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析 JSON 字符串，返回 JiFengBaseResp<T>
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型（如 String.class 或自定义 DTO）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> JiFengBaseResp<T> parseToJiFengResp(String jsonStr, Class<T> clazz) {
        try {
            assertResponseNotBlank(jsonStr);
            JiFengBaseResp<T> resp = JSON.parseObject(jsonStr, new TypeReference<JiFengBaseResp<T>>(clazz) {});
            if (resp == null) {
                throw new ThirdWarehouseEmptyResponseException(EMPTY_RESPONSE_MESSAGE);
            }
            return resp;
        } catch (ThirdWarehouseEmptyResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return JiFengBaseResp.error("JSON 解析失败,原始值：{}，异常: {}", jsonStr,e);
        }
    }

    /**
     * 解析 JSON 字符串（带泛型 TypeReference）
     * @param jsonStr JSON 字符串
     * @param typeRef 目标类型（如 new TypeReference<JiFengBaseResp<String>>() {}）
     * @return 成功返回解析结果，失败返回 errorResp
     */
    public static <T> JiFengBaseResp<T> parseToJiFengResp(String jsonStr, TypeReference<JiFengBaseResp<T>> typeRef) {
        try {
            assertResponseNotBlank(jsonStr);
            JiFengBaseResp<T> resp = JSON.parseObject(jsonStr, typeRef);
            if (resp == null) {
                throw new ThirdWarehouseEmptyResponseException(EMPTY_RESPONSE_MESSAGE);
            }
            return resp;
        } catch (ThirdWarehouseEmptyResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("JSON 解析失败,原始值：{}，异常: ", jsonStr,e);
            return JiFengBaseResp.error("JSON 解析失败,原始值：{}，异常:{} ", jsonStr,e);
        }
    }

}
