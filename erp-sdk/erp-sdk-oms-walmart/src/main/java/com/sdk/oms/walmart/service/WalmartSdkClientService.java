package com.sdk.oms.walmart.service;


import com.common.core.utils.OkHttpUtils;
import com.common.core.utils.UUID;
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
        String s = walmartSdkClientService.sendWalmartPostRefreshToeken(baseUrl, consumerId, clientId, clientSecret);
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
        param.put("grant_type", "authorization_code");
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
        param.put("refresh_token", "eyJraWQiOiI4OTljZWU0ZS00NTAwLTRjYTItOTNmMC0wYzExMDZmOWZlNWEiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..MVRtY67kPIG5wGpV.n8aq61jeTefcYxDeNUXdbJBmomWKKk9gOTJXjJrYJdRnmTFkBNN-mKEKrL-9hCbzBiRTiM48iFUONouZDiikydWA4xuDJtheFHo2cSi7Rt31EWHYLotLnyDAOtMKvVDF7YWq_UEJga9Jvb8koI1ORZkeN8_1bYqvhdK9wrMzndVNITbeS93sY-ABAzlPIx1jqw1-xjQd5cWppQg_fvkpUedfR1AJJxoZ7w7uqmvRrg5NOwu058KtElCxx64-nVpaq8q7Tim71Y5Xu9wr9wUAG6Q6CkB1mLyhnoyNr_09ssyLwFrvCU9cwvIheoIH23z-4yMF8wO2nHngogh1lOmuvjjy7nuJfpm8HmxroTPoxwfkSGDcn7m4edHIox2ZZTsh2TJy1ThfZQXDnszIGooDxQuAaz2fYHehB9R4fV1ZxZot9rpF2u_KjbklVgvLEufg_jRkgH1TyRQM1jcES0uw57F-9pi2fqGCysiBgvLa9DVNpn5_mCYemhCeuI81PnK0rV5kP3Ns3bd6Q8DkdqWzqQEOsEMpurVdwm3HJtwMvQt46oM7undMDLTPgRa4pB8MLDPNzdIftpjoqtET8TYwY1GJS2EB2Y2teGWf-22IpgpuboCkY_q_IV99VM3BTwRHQyGMKboawjDP-ZDPmxW6ZutJLZ25kYTzG8l33LW-C1nE3Yo0wJMsGdBbUP_u.5ou4B-gFK0xQBKBGLMUIpQ");
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headers);
        return bodyStr;
    }


    private String getclient(String clientId, String clientSecret) {
        StringBuffer sb = new StringBuffer();
        sb.append(clientId);
        sb.append(":");
        sb.append(clientSecret);
        String str = sb.toString();
        return str;
    }
}
