package com.erp.server.wms.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.TreeMap;

/**
 * 金蝶扩展API 工具类
 */
@Slf4j
@Component
public class KingdeeExtensionUtils {

    // 查询最新库存组织关账时间路径
    public static final String STK_CLOSE_PATH = "/open/api/stk/closed/list";


    // 通用参数名称
    public static final String API_KEY_NAME = "apiKey";
    public static final String TIMESTAMP_NAME = "timestamp";
    public static final String SIGN_NAME = "sign";
    public static final String HEADER_SYSTEM_NAME = "system";
    public static final String HEADER_SYSTEM = "erp";

    /**
     * 金蝶扩展项目链接
     */
    private static String HOST;

    /**
     * 金蝶扩展项目接口签名key
     */
    private static String API_KEY;

    /**
     * 金蝶扩展项目接口签名密钥
     */
    private static String API_SECRET;

    @Value("${extension.kingdee.host:http://127.0.0.1:18080}")
    public void setHost(String host) {
        KingdeeExtensionUtils.HOST = host;
    }

    @Value("${extension.kingdee.api-key:test}")
    public void setApiKey(String apiKey) {
        KingdeeExtensionUtils.API_KEY = apiKey;
    }

    @Value("${extension.kingdee.api-secret:test}")
    public void setApiSecret(String apiSecret) {
        KingdeeExtensionUtils.API_SECRET = apiSecret;
    }

    /**
     * 添加通用参数并签名和请求金蝶扩展项目
     */
    private static String postAndSign(String fullUrl, TreeMap<String, String> params) {
        params.put(API_KEY_NAME, API_KEY);
        params.put(TIMESTAMP_NAME, "" + System.currentTimeMillis());
        String sign = KingdeeExtensionSignUtil.sign(params, API_SECRET);
        params.put(SIGN_NAME, sign);
        try (HttpResponse response = HttpRequest.post(fullUrl)
                .body(JSONUtil.toJsonStr(params))
                // 添加请求头信息
                .header(HEADER_SYSTEM_NAME, HEADER_SYSTEM)
                .contentType("application/json")
                .timeout(60000)
                .execute()
        ) {
            if (!response.isOk()) {
                log.error("请求金蝶扩展项目接口失败: response={}", response.body());
                throw new ServiceException("请求金蝶扩展项目接口失败：error=" + response.body());
            }
            return new JSONObject(response.body()).getStr("data");
        }
    }


    /**
     * 查询最新库存组织关账时间列表
     */
    public static String queryStkClosedList() {
        return postAndSign(HOST.concat(STK_CLOSE_PATH), new TreeMap<>());
    }

    public static void main(String[] args) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put(API_KEY_NAME, "test");
        params.put(TIMESTAMP_NAME, "" + System.currentTimeMillis());
        String sign = KingdeeExtensionSignUtil.sign(params, "test", SIGN_NAME);
        params.put(SIGN_NAME, sign);

        String fullUrl = "http://127.0.0.1:18080".concat(STK_CLOSE_PATH);
        try (HttpResponse response = HttpRequest.post(fullUrl)
                .body(JSONUtil.toJsonStr(params))
                // 添加请求头信息
                .header(HEADER_SYSTEM_NAME, HEADER_SYSTEM)
                .contentType("application/json")
                .timeout(100000)
                .execute()
        ) {
            if (!response.isOk()) {
                log.error("请求金蝶扩展项目接口失败: response={}", response.body());
                throw new ServiceException("请求金蝶扩展项目接口失败：error=" + response.body());
            }
            String json = new JSONObject(response.body()).getStr("data");
            JSONArray data = new JSONArray(json);
            System.out.println(data);
        }
    }
}
