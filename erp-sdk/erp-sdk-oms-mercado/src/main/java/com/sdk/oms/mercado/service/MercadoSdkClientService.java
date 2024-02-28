package com.sdk.oms.mercado.service;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.oms.dto.ShopDTO;
import com.sdk.oms.mercado.dto.mercado.MercadoRefreshTokenDTO;
import com.sdk.oms.mercado.dto.mercado.MercadoTokenDTO;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 美客多平台SDK
 * @Author Luo_WG
 * @Date 2024/2/27 18:04
 **/
@Slf4j
@Component
public class MercadoSdkClientService {
    public static void main(String[] args) {
        String baseUrl = "https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&grant_type=authorization_code&client_id=3457166802805723&client_secret=F1L9EUIhsIlUc6yRGyzMhwFVweBZKIJ7&code=TG-65ddb75bae3ab00001ae1f68-1509269799&redirect_uri=https://erptest.ulanzi.cn:8020/store-permission-result";
        //入参（无）
        Map<String, Object> param = new HashMap<>();

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/x-www-form-urlencoded");
        headerMap.put("accept", "application/json");

        //发起POST请求
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);
        System.out.println(bodyStr);
    }

    /**
     * 发送请求到美客多获取token
     * @Author Luo_WG
     * @Date 2024/2/27 18:05
     * @param paramMap
     * @return com.sdk.oms.mercado.dto.mercado.MercadoTokenDTO
     **/
    public MercadoTokenDTO sendMercadoPostToken(Map<String, String> paramMap) {

        //组装授权url
        String clientId = paramMap.get("clientId");
        String clientSecret = paramMap.get("clientSecret");
        String redirectUri = paramMap.get("redirectUri");
        String url = paramMap.get("baseUrl");
        String code = paramMap.get("code");
        //https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
        String baseUrl = String.format(url, clientId, clientSecret, code, redirectUri);

        //入参（无）
        Map<String, Object> param = new HashMap<>();

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/x-www-form-urlencoded");
        headerMap.put("accept", "application/json");

        //发起POST请求
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);

        //解析数据
        MercadoTokenDTO tokenDTO = null;
        try {
            tokenDTO = JSONUtil.toBean(bodyStr, MercadoTokenDTO.class);
            log.info(String.format("::::: 美客多授权 ::::: 请求地址 => %s, 平台返回值 => %s ", baseUrl, tokenDTO));
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), bodyStr);
        }
        if (StringUtil.isBlank(tokenDTO.getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), bodyStr);
        }

        //返回token实体
        return tokenDTO;
    }

    public MercadoRefreshTokenDTO refreshToken(ShopDTO.RefreshTokenDTO dto) {

        //组装刷新token请求的url
        //https://api.mercadolibre.com/oauth/token?grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s
        String baseUrl = String.format(dto.getBaseUrl(), dto.getClientId(), dto.getClientSecret(), dto.getRefreshToken());

        //入参（无）
        Map<String, Object> param = new HashMap<>();

        //请求头（无）
        Map<String, String> headerMap = new HashMap<>();

        //发起POST请求
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);

        //解析数据
        MercadoRefreshTokenDTO refreshTokenDTO = null;
        try {
            refreshTokenDTO = JSONUtil.toBean(bodyStr, MercadoRefreshTokenDTO.class);
            log.info(String.format("::::: 美客多刷新token ::::: 请求地址 => %s, 平台返回值 => %s ", baseUrl, refreshTokenDTO));
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), bodyStr);
        }
        if (StringUtil.isBlank(refreshTokenDTO.getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), bodyStr);
        }

        //返回token实体
        return refreshTokenDTO;
    }
}
