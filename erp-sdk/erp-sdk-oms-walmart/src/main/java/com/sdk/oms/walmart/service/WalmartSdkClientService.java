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
        String baseUrl = "https://marketplace.walmartapis.com/v3/items";
        String consumerId = UUID.randomUUID().toString();
        String clientId = "2434a35c-7c42-4420-9618-0c179b68a8c2";
        String clientSecret = "AMW5lbVFqG2DMP4DuLezhSkbk4u0JLGUjdFlsrl_p0sagsBkYPPiQhRbEvkE4a6k6KXNKhB--RGlqPKIfhUoV28";
        String accessToken = "eyJraWQiOiI4OTljZWU0ZS00NTAwLTRjYTItOTNmMC0wYzExMDZmOWZlNWEiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..S1NGYGAKgCQEAJ_h.vrNHF1eA0cGU14e0icbNg9lwe0DI16EfNYg-Q_vKs23Chdqs2Kw-pfGJLbC289CE9VseDswus2RHrvyV6cWQagkT1fsqnLFvbGbV3q8ao00c72Hw6rleMJLBCAQ2flScfKdhyjgaj-d88Ib_AHOwpmEwx7McaGjiwimTeVQwYuOvI6VkLx-EcEpcuAEmSehIfk5bgkRrLVulkUnayBYadCGRTeJ3spnMSZa9zEAc6LFl0xqWj3BEE9AaQlZweauLgcuPksE6MwgIfK4y0HBiAogLPC7hgA666wne0Ewc26zKBg6sjPlTPRKn-20_auXf6KuHdTQIxAadDrdUxqhEnfZuIFo5Ku7_YNHNfwxzVgBgzHM8OcEpaq804oKtqaq6A52lYsWAqKBc200d_Kl_Cr3Kx6yNhERJ4M9beeW2wWHYGgDGnuI1xegA-g8gvkIm6lacLxPM4t9cGMhn1HpEZMZmqI4De3lRgn_C0yP93jH_LJu2VLJ5TQ9sJnhjup7EUM4I7cWdhJiecsOTB7UVlQvav5W6w_EJN7AGx_7cXqIXGtB0SfHJj0uMShwFv95dZo_jF-6pXoSw0DrBEAPt7mfrfl8lenNllo1j2o1f9HdJCLgkZMN9SQsAZG4aZZ202tplJ34nnTQjODzvd4TX9XLQKG4cLL5Hy-pE7K855BiJNCOT4M2aTHZElDST.w6DtgGOt7Besy8x9AOpCRA";
        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
//        String s = walmartSdkClientService.sendWalmartPostToeken(baseUrl, consumerId, clientId, clientSecret);
        String s = walmartSdkClientService.sendWalmartGet(baseUrl, consumerId, clientId, clientSecret, accessToken);
        System.out.println(s);
    }

    /**
     * 发送请求到沃尔玛获取令牌token
     * @param baseUrl 接口地址
     * @param consumerId 对此ID使用随机生成的GUID
     * @param clientId 账户id
     * @param clientSecret 账户秘钥
     * @return java.lang.String
     */
    public String sendWalmartPostToeken(String baseUrl, String consumerId, String clientId, String clientSecret) {
        Map<String, String> headers = new HashMap();
        headers.put("Content-Type", WalmartStaticKey.accept_application);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.VERSION", "1.0.0");
        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        Map<String, Object> param = new HashMap();
        param.put("grant_type", "client_credentials");
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headers);
        return bodyStr;
    }

    /**
     * 发送Get请求到沃尔玛
     * @param baseUrl 接口地址
     * @param consumerId 对此ID使用随机生成的GUID
     * @param clientId 账户id
     * @param clientSecret 账户秘钥
     * @param accessToken 短令牌
     * @return java.lang.String
     */
    public String sendWalmartGet(String baseUrl, String consumerId, String clientId,String clientSecret, String accessToken) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("Authorization", "Basic "+Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        headers.put("WM_SEC.ACCESS_TOKEN", accessToken);
        String bodyStr = OkHttpUtils.doGet(baseUrl, null, headers);
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
