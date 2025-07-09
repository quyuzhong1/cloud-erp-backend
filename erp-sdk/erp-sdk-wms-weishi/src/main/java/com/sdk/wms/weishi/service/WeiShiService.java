package com.sdk.wms.weishi.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.weishi.dto.request.WeiShiAuthRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiTokenResp;
import com.sdk.wms.weishi.dto.response.WeiShiWarehouseResp;
import com.sdk.wms.weishi.utils.WeiShiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Slf4j
@Component
public class WeiShiService {

    private final String preUrl = "http://218.17.123.141:808/prod-api";

    private final String api = "/omsapi/api";

    private final String apiUrl = preUrl + api;

    public static void main(String[] args) {
        WeiShiService weiShiService = new WeiShiService();
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("key","613cefbb29a34ab5af3f26c3a04ff6a7");
        authMap.put("accessToken","566362ec-97bf-430f-ab8e-9ee140253d8d");
        WeiShiBaseResp<List<WeiShiWarehouseResp>> weiShiBaseResp = weiShiService.getWarehouseList(authMap);
//        WeiShiBaseResp<WeiShiTokenResp> weiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(weiShiBaseResp));
    }

    public WeiShiBaseResp<WeiShiTokenResp> accessToken(Map<String,Object> authMap){
        String path = "/omsapi/auth/omsLoginBySecretKey/" + authMap.get("key").toString();
        String bodyStr = OkHttpUtils.doGet(preUrl +path, new HashMap<>(), new HashMap<>());
        return WeiShiUtils.parseToJiFengResp(bodyStr, WeiShiTokenResp.class);
    }


    /**
     * 查询仓库
     * @param authMap
     * @return
     */
    public WeiShiBaseResp<List<WeiShiWarehouseResp>> getWarehouseList(Map<String,Object> authMap){
        String action = "getAllWarehouse";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", "{}");
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<List<WeiShiWarehouseResp>>>() {});
    }


    private Map<String, String> buildHearderMap(Map<String, Object> authMap) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authMap.get("accessToken").toString());
        return headerMap;
    }

}
