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
        String accessToken = "eyJraWQiOiI4OTljZWU0ZS00NTAwLTRjYTItOTNmMC0wYzExMDZmOWZlNWEiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..wRtEqEa_aYmM6b6j.zF5tkLMqHZipx9bdnmN1HwHGii7kThcYgiEAJfFWKFAwSPXlUNymw-JLeK2_sC_Ye9oqKMXcumzcrzfO1b4_JxqA7LOlB2HSSO0fkYGrdSIpmLSj9KiibJWwnoUyRzKoZQdwRzWeSswW-oDqo4sFB2JbCTfB9xMURP-52N42w7ffgr-GpxNx6cXTdvlSSxGaARpYdi1YJARaRbNuwE7q1qHBUCnswsF7eeQ1hlM7LJMGUmvFyI7q0iK2BmblzPRGKz18hdMqFUzVosigbGYY_3KLHIgguwmA9D3W0zRMmUttbiRzHLPTJI1hiF_RK2uLwekWo8Jqw0dFuezyfUYcejB-7DeSBsNZ1dbxbI3oYFSMMvV9SylCU-Ba9BiR4KIe8SWy3i9W1jFns-t2nbVdsES_DpcPsd0eYPnqQDErOgXMv6e_OXHXss_89l2HlAYr0zgDJNcguedXVMR7P8unbQNozYtz6XuyBLLfA1WR3l_z0uKv92V3XxE6lCKXwd8wg2jCzxtWVc6rdtyBJbByRMsFU-kPMb804qZH-y3fprSbjgo_eKzC1w7rXavdw4Q-jp_EXDKyu_JS_L1UT1pTOyfNQMBfku0xl-dA2CdHmjYueNPaJdAo3_G89op9MOwB-xVV8lBkFKdusRZgnuRAuPSLTnymVRcvInIjABCzNsExLo-ed5Y1YAsO4SSm.ni2ViUuzj6teOJfP88c43A";
        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
//        WalmartTokenDTO s = walmartSdkClientService.sendWalmartPostToken(baseUrl, clientId, clientSecret);
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
