package com.erp.server.dmp.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

@Slf4j
public class RestCloudApiUtil {
    private RestCloudApiUtil() {
    }

    private static String restcloudUrl = SpringUtil.getProperty("restcloud.url");

    private static String restcloudPort = SpringUtil.getProperty("restcloud.port");

    public static boolean reCreate(String checkMonth, String... urls) {
        boolean resultBool = false;
        for (String url : urls) {
            Map<String, Object> map = new HashMap<>();
            map.put("data", Arrays.asList());
            map.put("yearMonth", checkMonth);
            resultBool = requestRestCloud(url, map, resultBool);
        }
        return resultBool;
    }


    /**
     * 异步调用
     * @param checkMonth 核对月份
     * @param urls 请求路径列表
     * @return 是否成功，true表示至少有一个接口调用成功，false表示任意接口调用失败
     */
    public static boolean syncReCreate(String checkMonth, String... urls) {
        for (String url : urls) {
            Map<String, Object> map = new HashMap<>();
            map.put("data", Arrays.asList());
            map.put("yearMonth", checkMonth);
            CompletableFuture.runAsync(() -> {
                try {
                    requestRestCloud(url, map, true);
                } catch (Exception e) {
                    log.error("异步调用谷云接口异常，url: {}，异常: {}", url, e.getMessage(), e);
                }
            });
        }
        return true;
    }

    public static boolean requestRestCloud(String url, Map<String, Object> map, boolean resultBool) {
        String restUrl = "http://" + restcloudUrl + ":" + restcloudPort + "/restcloud/" + url;
        HttpResponse response = HttpRequest.post(restUrl)
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用谷云地址：" + restUrl + "状态码" + response.getStatus() + "错误，请联系实施");
        } else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Integer resultCode = responseJson.getInteger("resultCode");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != resultCode && 1 == resultCode) {
                JSONArray jsonArray = responseJson.getJSONArray("data");
                if (CollUtil.isNotEmpty(jsonArray)) {
                    resultBool = true;
                } else {
                    String dataTotal = responseJson.getString("dataTotal");
                    if (StringUtils.isNotBlank(dataTotal) && Stream.of(dataTotal.split(",")).anyMatch(s -> !s.trim().equals("0"))) {
                        resultBool = true;
                    }
                }
            } else {
                throw new ServiceException("调用谷云地址：" + restUrl + "返回报文：" + body + "错误，请联系实施");
            }
        }
        return resultBool;
    }


    /**
     * 异步调用谷云接口，适用于不关心结果的场景
     * @param url 请求路径
     * @param map 请求参数
     */
    public static boolean syncRequestRestCloud(String url, Map<String, Object> map) {
        CompletableFuture.runAsync(() -> {
            String restUrl = "http://" + restcloudUrl + ":" + restcloudPort + "/restcloud/" + url;
            HttpResponse response = HttpRequest.post(restUrl)
                    .header("Content-Type", "application/json")
                    .body(JSON.toJSONString(map))
                    .timeout(60000)
                    .execute();
            if (200 != response.getStatus()) {
                log.error("调用谷云地址：{}状态码{}错误，请联系实施", restUrl, response.getStatus());
            } else {
                String body = response.body();
                JSONObject responseJson = JSON.parseObject(body);
                Integer resultCode = responseJson.getInteger("resultCode");
                // 判断结果异常:ETLProcessRunResultCode
                if (null != resultCode && 1 == resultCode) {
                    log.warn("调用谷云地址：{}返回报文：{}成功", restUrl, body);
                } else {
                    log.error("调用谷云地址：{}返回报文：{}错误，请联系实施", restUrl, body);
                }
            }
        });
        return true;
    }
}
