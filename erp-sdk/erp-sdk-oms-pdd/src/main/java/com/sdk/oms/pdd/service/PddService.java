package com.sdk.oms.pdd.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.pdd.dto.AccessTokenResponse;
import com.sdk.oms.pdd.dto.AuthTokenCreateResponse;
import com.sdk.oms.pdd.dto.AuthTokenRefreshTokenResponse;
import com.sdk.oms.pdd.util.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * pdd
 **/
@Slf4j
@Component
public class PddService {

    @Resource
    private ShopInfoFeign shopInfoFeign;
//    public static void main(String[] args) throws NoSuchAlgorithmException {
//        PddService pddService = new PddService();
//        ShopDTO.RefreshTokenDTO refreshTokenDTO = new ShopDTO.RefreshTokenDTO();
//        refreshTokenDTO.setRefreshToken("cc22864e9b3b4a45898c23812bd6c521c8bfde79");
//        refreshTokenDTO.setClientId("3becefd1b37748e2ae24065f272c6c66");
//        refreshTokenDTO.setBaseUrl("https://gw-api.pinduoduo.com/api/router");
//        refreshTokenDTO.setClientSecret("f9965a90a6decabedf5af229aa50f4129d71738d");
//        pddService.refreshToken(refreshTokenDTO);
//    }
    public static void main(String[] args) throws NoSuchAlgorithmException {
        long timestamp = System.currentTimeMillis()/1000;
        long queryTimeStart = timestamp - 7*24*60*60;
        long queryTimeEnd = timestamp - 7*24*60*60 + 60*60;
        Map<String,Object> params = new HashMap<>();
        params.put("type","pdd.erp.oub.list.get");
        params.put("client_id","3becefd1b37748e2ae24065f272c6c66");
        params.put("access_token","08090b4534ab4ceb8777d418c168449fa9f2a23b");
        params.put("timestamp",timestamp);
        params.put("target_client_id", "e6c4b8a127c14831b2ff860735cf8680");
        params.put("page",1);
        params.put("page_size",100);
        params.put("sid","wjkj03");
        params.put("query_time_type",2);
        params.put("query_time_start",queryTimeStart);
        params.put("query_time_end",queryTimeEnd);
        String sign = EncryptionUtils.generateSign(params, "f9965a90a6decabedf5af229aa50f4129d71738d");
        params.put("sign",sign);
        System.out.println(JSONObject.toJSONString(params));
        String response = OkHttpUtils.doPostJson("https://ark-api.pinduoduo.com/ark/router", params, new HashMap<>());
        System.out.println(response);
    }

    public AuthTokenCreateResponse.PopAuthTokenCreateResponse createToken(Map<String, String> paramMap)  {

        String clientId = paramMap.get("clientId");
        String clientSecret = paramMap.get("clientSecret");
        String authCode = paramMap.get("code");
        String url = paramMap.get("baseUrl");
        String apiName = "pdd.pop.auth.token.create";
        Map<String,String> headerMap = new HashMap<>();
        long timestamp = System.currentTimeMillis();
        Map<String,Object> params = new HashMap<>();
        params.put("type",apiName);
        params.put("client_id",clientId);
        params.put("timestamp",timestamp);
        params.put("code",authCode);
        String sign;
        // 生成签名
        try {
            sign = EncryptionUtils.generateSign(params, clientSecret);
        }catch (Exception e){
            throw new ServiceException("拼多多获取accessToken失败，签名失败",e);
        }
        params.put("sign",sign);
        String response = OkHttpUtils.doPostJson(url, params, headerMap);
        AuthTokenCreateResponse authTokenCreateResponse = JSON.parseObject(response,AuthTokenCreateResponse.class);
        if(Objects.isNull(authTokenCreateResponse)){
            throw new ServiceException("拼多多获取accessToken失败，response="+response);
        }
        if(Objects.nonNull(authTokenCreateResponse.getErrorResponse())){
            throw new ServiceException("拼多多获取accessToken失败,{}", JSONUtil.toJsonStr(authTokenCreateResponse.getErrorResponse()));
        }
        return authTokenCreateResponse.getPopAuthTokenCreateResponse();
    }

    public AuthTokenRefreshTokenResponse.PopAuthTokenRefreshResponse refreshToken(ShopDTO.RefreshTokenDTO refreshTokenDTO) {

        String clientId = refreshTokenDTO.getClientId();
        String clientSecret = refreshTokenDTO.getClientSecret();
        String refreshToken = refreshTokenDTO.getRefreshToken();
        String url = refreshTokenDTO.getBaseUrl();
        String apiName = "pdd.pop.auth.token.refresh";
        Map<String,String> headerMap = new HashMap<>();
        long timestamp = System.currentTimeMillis();
        Map<String,Object> params = new HashMap<>();
        params.put("type",apiName);
        params.put("client_id",clientId);
        params.put("timestamp",timestamp);
        params.put("refresh_token",refreshToken);
        String sign;
        // 生成签名
        try {
            sign = EncryptionUtils.generateSign(params, clientSecret);
        }catch (Exception e){
            throw new ServiceException("拼多多获取accessToken失败，签名失败",e);
        }
        params.put("sign",sign);
        String response = OkHttpUtils.doPostJson(url, params, headerMap);
        AuthTokenRefreshTokenResponse result = JSON.parseObject(response,new TypeReference<AuthTokenRefreshTokenResponse>() {}.getType());
        if(Objects.isNull(result)){
            throw new ServiceException("拼多多刷新accessToken失败，response="+response);
        }
        if(Objects.nonNull(result.getErrorResponse())){
            throw new ServiceException("拼多多获取accessToken失败,{}", JSONUtil.toJsonStr(result.getErrorResponse()));
        }
        return result.getPopAuthTokenRefreshResponse();
    }
}
