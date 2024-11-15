package com.sdk.tms.shopee.utils;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.sdk.tms.shopee.model.base.BaseResponse;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zdy
 * @ClassName ShopeeBaseService

 * @date 2023年10月20日
 * @version: 1.0
 */
@Slf4j
public class ShopeeApiUtils {
    
    private ShopeeApiUtils(){}
    
    private static String CONTENT_TYPE = "Content-Type";
    private static String APPLICATION = "application/json";
            

    public static String getOrderSign(String path, String accessToken, long partnerId, String tmpPartnerKey, long shopId) {
        long timest = System.currentTimeMillis() / 1000L;
        String tmpBaseString = String.format("%s%s%s%s%s", partnerId, path, timest, accessToken, shopId);
        byte[] partnerKey;
        byte[] baseString;
        String sign = null;
        try {
            baseString = tmpBaseString.getBytes("UTF-8");
            partnerKey = tmpPartnerKey.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(partnerKey, "HmacSHA256");
            mac.init(secretKey);
            sign = String.format("%064x", new BigInteger(1, mac.doFinal(baseString)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    /**
     * GET 请求
     *
     * @param baseUrl
     * @param paramMap
     * @return
     */
    public static BaseResponse sendGet(String baseUrl, Map<String, Object> paramMap) {
        BaseResponse resultMap = null;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Connection", "keep-alive");
        try {
            String bodyStr = OkHttpUtils.doGet(baseUrl, paramMap, headers);
            resultMap = JSON.parseObject(bodyStr, BaseResponse.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return resultMap;
    }

    /**
     * 虾皮标记发货 post请求
     *
     * @param baseUrl 接口地址
     * @param paramsJson
     * @return java.lang.String
     */
    public static String sendPostBase64(String baseUrl, Map<String, Object> urlParams, String paramsJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Accept", APPLICATION);
        String url = buildUrl(baseUrl, urlParams);
        log.info("url：{}", url);
        try {
            String bodyStr = OkHttpUtils.doPostJsonBase64(url, paramsJson, headers);
            log.info("bodyStr：{}", bodyStr);
            return bodyStr;
        } catch (Exception e) {
            log.error("请求异常：{}", e.getMessage());
            throw new ServiceException("虾皮面单获取异常"+e.getMessage());
        }
    }

    /**
     * 虾皮标记发货 post请求
     *
     * @param baseUrl 接口地址
     * @param paramsJson
     * @return java.lang.String
     */
    public static BaseResponse sendPost(String baseUrl, Map<String, Object> urlParams, String paramsJson) {
        BaseResponse resultMap = null;
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Accept", APPLICATION);
        String url = buildUrl(baseUrl, urlParams);
        log.info("url：{}", url);
        try {
            String bodyStr = OkHttpUtils.doPostJson(url, paramsJson, headers);
            log.info("bodyStr：{}", bodyStr);
            resultMap = JSONUtil.toBean(bodyStr, BaseResponse.class);

        } catch (Exception e) {
            log.error("请求异常：{}", e.getMessage());
        }
        return resultMap;
    }

    public static String buildUrl(String url, Map<String, Object> urlParams) {
        StringBuilder urlBuilder = new StringBuilder(url);
        if (urlParams != null && urlParams.size() > 0) {
            int i = 1;
            for (Map.Entry<String, Object> entry : urlParams.entrySet()) {
                if (1 == i) {
                    urlBuilder.append("?" + entry.getKey() + "=" + entry.getValue());
                    i += 1;
                } else {
                    urlBuilder.append("&" + entry.getKey() + "=" + entry.getValue());
                }

            }
        }
        return urlBuilder.toString();
    }
}
