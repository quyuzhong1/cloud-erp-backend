package com.sdk.oms.walmart.service;


import com.common.core.utils.OkHttpUtils;
import com.common.core.utils.UUID;
import com.sdk.oms.walmart.api.WalmartSign;
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

    public static void main(String[] args) {
        String baseUrl = "https://marketplace.walmartapis.com/v3/token";
        String consumerId = UUID.randomUUID().toString();
        String clientId = "2434a35c-7c42-4420-9618-0c179b68a8c2";
        String clientSecret = "AMW5lbVFqG2DMP4DuLezhSkbk4u0JLGUjdFlsrl_p0sagsBkYPPiQhRbEvkE4a6k6KXNKhB--RGlqPKIfhUoV28";
        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
        String s = walmartSdkClientService.sendWalmartPostToeken(baseUrl, consumerId, clientId, clientSecret);
        System.out.println(s);
    }

    /**
     * 发送请求到沃尔玛
     * @param baseUrl
     * @param consumerId 对此ID使用随机生成的GUID
     * @param clientId
     * @param clientSecret
     * @return java.lang.String
     */
    public String sendWalmartPostToeken(String baseUrl, String consumerId, String clientId, String clientSecret) {
        Map<String, String> headers = new HashMap();
        headers.put("Content-Type", WalmartStaticKey.accept_application);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.VERSION", "1.0.0");
//        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));

        Map<String, Object> param = new HashMap();
        param.put("grant_type", "client_credentials");
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headers);
        return bodyStr;
    }

    /**
     * 发送请求到沃尔玛
     * @param baseUrl
     * @param consumerId 对此ID使用随机生成的GUID
     * @param clientId
     * @param clientSecret
     * @return java.lang.String
     */
    public String sendWalmartPostCode(String baseUrl, String consumerId, String clientId, String clientSecret) {
        Map<String, String> headers = new HashMap();
        headers.put("Content-Type", WalmartStaticKey.accept_application);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.VERSION", "1.0.0");
//        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));

        Map<String, Object> param = new HashMap();
        param.put("grant_type", "authorization_code");
        param.put("grant_type", "eyJraWQiOiI4OTljZWU0ZS00NTAwLTRjYTItOTNmMC0wYzExMDZmOWZlNWEiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..hyqrkig7QOo_HAZy.n1E9sIpicLinNVhTLC-2IFjQ_S7nKnTcvDbPBOU-CD084dHmUvQht6sKKp1UNKug6cOntj03Shko8vxpjohnanmsbT-CHBZxyjI_kTGhtMk_g4hU8DOtBcddI3Pql794utv-SIFFCnZGQjDUz1GAF5OAsBkLKT6ctlcYAGcULENBqI_rxMbNuiZQa-GIZqBNNe3CJtunkZ-vcwdXP8FY7EBJHGKNLbDk5dESQKdy64QQ6QZvJZWW_ccPLA23URXgBLHgg3umATj65jpysB2Hti1r-rtyFqvZHWo1p1Lv4sxjORi8t5XNF9SI9OHRWPkz8Ic8vguBQgmrpC7veV8MuCkUS_xUIOIE-gDb-A8M9rFtP22d4nlnxyunsl6ot32OkBrd7pX50NcbWROHqEERgs1w-UpRhq6jmYblYJwoOcN3Vc3_MHmKO2ZV16dftc_3bkBd_oLdMsFbzPUNnlKbAtAF2jUHTelXeZy8lrrSgMcN-xOswIGyMcF1iYpbwA3_p2oPeDFfBuL7anATn8eiTkAEP3sMvNKlYVCEXhenZUsMM-OHEiCZ0zXO1oaLHlq_rlXU7cCUuNQWOcjDqBUM5eX5RfNPD23TltXVYWvuMcUF46WvrrCzYze3ueR2jWZA7D3gUcl6nEUcwSQpZo9935VYZI11TvAF-mGjliJhDpadNbIpN29d2Q2lhzUk.ca5mN8q74ErIwi9AgHWQSQ");
        String bodyStr = OkHttpUtils.doPost("", param, headers);
        return bodyStr;
    }

    /**
     * 发送请求到沃尔玛
     * @param baseUrl
     * @param consumerId 对此ID使用随机生成的GUID
     * @param clientId
     * @param clientSecret
     * @return java.lang.String
     */
    public String sendWalmartPostRefreshToeken(String baseUrl, String consumerId, String clientId, String clientSecret) {
        Map<String, String> headers = new HashMap();
        headers.put("Content-Type", WalmartStaticKey.accept_application);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.VERSION", "1.0.0");
//        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));

        Map<String, Object> param = new HashMap();
        param.put("grant_type", "refresh_token");
        param.put("refresh_token", "eyJraWQiOiI4OTljZWU0ZS00NTAwLTRjYTItOTNmMC0wYzExMDZmOWZlNWEiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..hyqrkig7QOo_HAZy.n1E9sIpicLinNVhTLC-2IFjQ_S7nKnTcvDbPBOU-CD084dHmUvQht6sKKp1UNKug6cOntj03Shko8vxpjohnanmsbT-CHBZxyjI_kTGhtMk_g4hU8DOtBcddI3Pql794utv-SIFFCnZGQjDUz1GAF5OAsBkLKT6ctlcYAGcULENBqI_rxMbNuiZQa-GIZqBNNe3CJtunkZ-vcwdXP8FY7EBJHGKNLbDk5dESQKdy64QQ6QZvJZWW_ccPLA23URXgBLHgg3umATj65jpysB2Hti1r-rtyFqvZHWo1p1Lv4sxjORi8t5XNF9SI9OHRWPkz8Ic8vguBQgmrpC7veV8MuCkUS_xUIOIE-gDb-A8M9rFtP22d4nlnxyunsl6ot32OkBrd7pX50NcbWROHqEERgs1w-UpRhq6jmYblYJwoOcN3Vc3_MHmKO2ZV16dftc_3bkBd_oLdMsFbzPUNnlKbAtAF2jUHTelXeZy8lrrSgMcN-xOswIGyMcF1iYpbwA3_p2oPeDFfBuL7anATn8eiTkAEP3sMvNKlYVCEXhenZUsMM-OHEiCZ0zXO1oaLHlq_rlXU7cCUuNQWOcjDqBUM5eX5RfNPD23TltXVYWvuMcUF46WvrrCzYze3ueR2jWZA7D3gUcl6nEUcwSQpZo9935VYZI11TvAF-mGjliJhDpadNbIpN29d2Q2lhzUk.ca5mN8q74ErIwi9AgHWQSQ");
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headers);
        return bodyStr;
    }

/*    *//**
     * 发送请求到沃尔玛
     * @param baseUrl
     * @param consumerId 对此ID使用随机生成的GUID
     * @param clientId
     * @param clientSecret
     * @return java.lang.String
     *//*
    public String sendWalmartPostRefreshToeken(String baseUrl, String consumerId, String clientId, String clientSecret) {
        WalmartSign walmartSign = new WalmartSign(consumerId, "https://marketplace.walmartapis.com/v3/items", privateEncodedStr);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-Type", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", WalmartStaticKey.CORRELATION_ID);
        headers.put("WM_SEC.TIMESTAMP", walmartSign.getTimestamp());
        headers.put("WM_SEC.AUTH_SIGNATURE", walmartSign.getSign("POST"));
        headers.put("WM_CONSUMER.CHANNEL.TYPE", channelType);
        headers.put("WM_CONSUMER.ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        if(sslClient == null)sslClient = new SSLClient();
        return sslClient.doPost(walmartSign.getBaseUrl(), headers, null, entity, 1000*60);
        return bodyStr;
    }*/


    private String getclient(String clientId, String clientSecret) {
        StringBuffer sb = new StringBuffer();
        sb.append(clientId);
        sb.append(":");
        sb.append(clientSecret);
        String str = sb.toString();
        return str;
    }
}
