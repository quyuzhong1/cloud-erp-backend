package com.sdk.wms.jifeng.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengTokenResp;
import com.sdk.wms.jifeng.utils.JiFengUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class JiFengService {

    public static void main(String[] args) {
        String clientId = "a03b35bf7f0c4c4f8e23e0599b5be649";
        String clientSecret = "f9af8dc7afea488991a216485987746c";
        String email = "wuliubu@ulanzi.cn";
        String token = "fbcea272c37b43d59376a48d0be197cd";
        String url = "https://sureparcel.jfwms.com";
        String refreshToken = "201fcb2fc2c343188149c085b6c87c53";
        Integer userId = 7471;
        JiFengService jiFengService = new JiFengService();
        JiFengBaseResp<JiFengTokenResp> resp = jiFengService.refreshToken(JiFengAuthRequest.builder().userId(userId).refreshToken(refreshToken).token(token).email(email).domain(url).clientId(clientId).clientSecret(clientSecret).key("vLdBchPpgi").build());
        System.out.println(resp);
    }

    public JiFengBaseResp<String> authorize(JiFengAuthRequest jiFengAuthRequest){
        String path = "/api/oauth/authorize";
        String url = jiFengAuthRequest.getDomain();

        Map<String, Object> param = new HashMap<>();
        param.put("clientId",jiFengAuthRequest.getClientId());
        param.put("email",jiFengAuthRequest.getEmail());
        param.put("token",jiFengAuthRequest.getToken());
        param.put("domain",url);
        String bodyStr = OkHttpUtils.doGet(url+path, param, new HashMap<>());
        return JiFengUtils.parseToJiFengResp(bodyStr, String.class);
    }

    public JiFengBaseResp<JiFengTokenResp> accessToken(JiFengAuthRequest jiFengAuthRequest){
        String path = "/api/oauth/accessToken";
        String url = jiFengAuthRequest.getDomain();

        Map<String, Object> param = new HashMap<>();
        param.put("clientId",jiFengAuthRequest.getClientId());
        param.put("clientSecret",jiFengAuthRequest.getClientSecret());
        param.put("key",jiFengAuthRequest.getKey());
        String bodyStr = OkHttpUtils.doGet(url+path, param, new HashMap<>());
        return JiFengUtils.parseToJiFengResp(bodyStr, JiFengTokenResp.class);
    }
    public JiFengBaseResp<JiFengTokenResp> refreshToken(JiFengAuthRequest jiFengAuthRequest){
        String path = "/api/oauth/refreshToken";
        String url = jiFengAuthRequest.getDomain();

        Map<String, Object> param = new HashMap<>();
        param.put("clientId",jiFengAuthRequest.getClientId());
        param.put("clientSecret",jiFengAuthRequest.getClientSecret());
        param.put("refreshToken",jiFengAuthRequest.getRefreshToken());
        param.put("userId",jiFengAuthRequest.getUserId());
        String bodyStr = OkHttpUtils.doGet(url+path, param, new HashMap<>());
        return JiFengUtils.parseToJiFengResp(bodyStr, JiFengTokenResp.class);
    }
}
