package com.sdk.oms.walmart.service;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.OkHttpUtils;
import com.common.core.utils.UUID;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.WalmartShopInfoDTO;
import com.sdk.oms.walmart.dto.WalmartTokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
        String clientId = "2434a35c-7c42-4420-9618-0c179b68a8c2";
        String clientSecret = "AMW5lbVFqG2DMP4DuLezhSkbk4u0JLGUjdFlsrl_p0sagsBkYPPiQhRbEvkE4a6k6KXNKhB--RGlqPKIfhUoV28";
        String accessToken = "eyJraWQiOiI4OTljZWU0ZS00NTAwLTRjYTItOTNmMC0wYzExMDZmOWZlNWEiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..S1NGYGAKgCQEAJ_h.vrNHF1eA0cGU14e0icbNg9lwe0DI16EfNYg-Q_vKs23Chdqs2Kw-pfGJLbC289CE9VseDswus2RHrvyV6cWQagkT1fsqnLFvbGbV3q8ao00c72Hw6rleMJLBCAQ2flScfKdhyjgaj-d88Ib_AHOwpmEwx7McaGjiwimTeVQwYuOvI6VkLx-EcEpcuAEmSehIfk5bgkRrLVulkUnayBYadCGRTeJ3spnMSZa9zEAc6LFl0xqWj3BEE9AaQlZweauLgcuPksE6MwgIfK4y0HBiAogLPC7hgA666wne0Ewc26zKBg6sjPlTPRKn-20_auXf6KuHdTQIxAadDrdUxqhEnfZuIFo5Ku7_YNHNfwxzVgBgzHM8OcEpaq804oKtqaq6A52lYsWAqKBc200d_Kl_Cr3Kx6yNhERJ4M9beeW2wWHYGgDGnuI1xegA-g8gvkIm6lacLxPM4t9cGMhn1HpEZMZmqI4De3lRgn_C0yP93jH_LJu2VLJ5TQ9sJnhjup7EUM4I7cWdhJiecsOTB7UVlQvav5W6w_EJN7AGx_7cXqIXGtB0SfHJj0uMShwFv95dZo_jF-6pXoSw0DrBEAPt7mfrfl8lenNllo1j2o1f9HdJCLgkZMN9SQsAZG4aZZ202tplJ34nnTQjODzvd4TX9XLQKG4cLL5Hy-pE7K855BiJNCOT4M2aTHZElDST.w6DtgGOt7Besy8x9AOpCRA";
        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
//        String s = walmartSdkClientService.sendWalmartPostToeken(baseUrl, consumerId, clientId, clientSecret);
        String s = walmartSdkClientService.sendWalmartGet(baseUrl, clientId, clientSecret, accessToken);
        System.out.println(s);
    }

    private static RedisUtil redisUtil;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil){
        this.redisUtil = redisUtil;
    }

    /**
     * 发送请求到沃尔玛获取令牌token
     * @param baseUrl 接口地址
     * @param clientId 账户id
     * @param clientSecret 账户秘钥
     * @return java.lang.String
     */
    public WalmartTokenDTO sendWalmartPostToken(String baseUrl, String clientId, String clientSecret) {
        String consumerId = UUID.randomUUID().toString();
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
        WalmartTokenDTO tokenDTO = JSONUtil.toBean(bodyStr, WalmartTokenDTO.class);
        return tokenDTO;
    }

    /**
     * 发送Get请求到沃尔玛
     * @param baseUrl 接口地址
     * @param clientId 账户id
     * @param clientSecret 账户秘钥
     * @param accessToken 短令牌
     * @return java.lang.String
     */
    public String sendWalmartGet(String baseUrl, String clientId,String clientSecret, String accessToken) {
        Map<String, String> headers = new HashMap<String, String>();
        String consumerId = UUID.randomUUID().toString();
        headers.put("Content-Type", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("Authorization", "Basic "+Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        headers.put("WM_SEC.ACCESS_TOKEN", accessToken);
        String bodyStr = OkHttpUtils.doGet(baseUrl, null, headers);
        return bodyStr;
    }

    /**
     * 获取Token
     */
    public static WalmartShopInfoDTO getTokenByShopId(String shopId) {
        // platform-token:平台名称:店铺ID
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.WALMART.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof WalmartShopInfoDTO) {
                return (WalmartShopInfoDTO) tokenObj;
            }
        }
        return null;
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
