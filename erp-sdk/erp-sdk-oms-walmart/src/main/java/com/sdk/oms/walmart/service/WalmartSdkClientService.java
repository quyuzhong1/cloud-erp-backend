package com.sdk.oms.walmart.service;


import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 沃尔玛服务
 * @Author Luo_WG
 * @Date 2023/10/16 14:50
 **/
@Slf4j
@Component
public class WalmartSdkClientService {


    /**
     * 发送请求到沃尔玛
     * @param baseUrl
     * @param consumerId 对此ID使用随机生成的GUID
     * @param clientId
     * @param clientSecret
     * @return java.lang.String
     */
    private String sendWalmartPostToeken(String baseUrl, String consumerId, String clientId, String clientSecret) {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-Type", WalmartStaticKey.accept_application);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.VERSION", "1.0.0");
        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        Map<String, String> param = new HashMap();
        param.put("grant_type", "client_credentials");
        String bodyStr = OkHttpUtils.doPost(baseUrl, headers, param);
        return bodyStr;
    }


    private String getclient(String client_id, String client_secret) {
        StringBuffer sb = new StringBuffer();
        sb.append(client_id);
        sb.append(":");
        sb.append(client_secret);
        String str = sb.toString();
        return str;
    }
}
