package com.sdk.oms.magalu.service;

import com.alibaba.fastjson.JSON;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.magalu.dto.MagaluTokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MagaluService {

    private static final String TOKEN_PATH = "/oauth/token";

    public MagaluTokenDTO createToken(Map<String, String> paramMap) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("grant_type", "authorization_code");
        params.put("client_id", paramMap.get("clientId"));
        params.put("client_secret", paramMap.get("clientSecret"));
        params.put("redirect_uri", paramMap.get("redirectUri"));
        params.put("code", paramMap.get("code"));

        return requestToken(paramMap.get("baseUrl"), params, "获取accessToken");
    }

    public MagaluTokenDTO refreshToken(Map<String, String> paramMap) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("grant_type", "refresh_token");
        params.put("client_id", paramMap.get("clientId"));
        params.put("client_secret", paramMap.get("clientSecret"));
        params.put("refresh_token", paramMap.get("refreshToken"));
        if (StringUtils.isNotBlank(paramMap.get("redirectUri"))) {
            params.put("redirect_uri", paramMap.get("redirectUri"));
        }

        return requestToken(paramMap.get("baseUrl"), params, "刷新token");
    }

    private MagaluTokenDTO requestToken(String baseUrl, Map<String, Object> params, String action) {
        String url = trimEndSlash(baseUrl) + TOKEN_PATH;
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        headerMap.put("Accept", "application/json");

        String response = OkHttpUtils.doPost(url, params, headerMap);
        MagaluTokenDTO tokenDTO = JSON.parseObject(response, MagaluTokenDTO.class);
        if (tokenDTO == null || StringUtils.isBlank(tokenDTO.getAccessToken())) {
            throw new ServiceException("Magalu" + action + "失败,response=" + response);
        }
        return tokenDTO;
    }

    private String trimEndSlash(String url) {
        if (StringUtils.isBlank(url)) {
            throw new ServiceException("Magalu授权地址未配置");
        }
        return StringUtils.removeEnd(url, "/");
    }
}
