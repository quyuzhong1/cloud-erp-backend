package com.sdk.oms.tictok.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.tictok.dto.tiktok.token.PlatformTikTokTokenDTO;
import com.sdk.oms.tictok.dto.tiktok.token.TokenDTO;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 美客多平台SDK
 * @Author Luo_WG
 * @Date 2024/2/27 18:04
 **/
@Slf4j
@Component
public class TikTokSdkClientService {
    public static void main(String[] args) {

    }


    private static RedisUtil redisUtil;


    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        TikTokSdkClientService.redisUtil = redisUtil;
    }


    public TokenDTO sendTikTokPostToken(Map<String, String> paramMap) {

        //组装授权url
        String clientId = paramMap.get("clientId");
        String clientSecret = paramMap.get("clientSecret");
        String authCode = paramMap.get("code");
        String url = paramMap.get("baseUrl");

        String path = "/api/v2/token/get?app_key=%s&auth_code=%s&app_secret=%s&grant_type=authorized_code";
        //https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
        String baseUrl = String.format(url+path, clientId, authCode, clientSecret);

        //入参（无）
        Map<String, Object> params = new HashMap<>();

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/x-www-form-urlencoded");
        headerMap.put("accept", "application/json");

        //发起POST请求
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok授权失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok授权失败，返回值 responseMap={}",
                    baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        PlatformTikTokTokenDTO tikTokTokenDTO = null;
        try {
            tikTokTokenDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), PlatformTikTokTokenDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }

        if (StringUtil.isBlank(tikTokTokenDTO.getData().getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.TIK_TOK.getName(), JSONUtil.toJsonStr(apiResult));
        }

        //返回token实体
        return tikTokTokenDTO.getData();
    }


    public TokenDTO refreshToken(ShopDTO.RefreshTokenDTO refreshTokenDTO) {

        //组装授权url
        String path = "/api/v2/token/refresh?app_key=%s&app_secret=%s&refresh_token=%s&grant_type=refresh_token";
        //https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
        String baseUrl = String.format(refreshTokenDTO.getBaseUrl()+path, refreshTokenDTO.getClientId(), refreshTokenDTO.getClientSecret(), refreshTokenDTO.getRefreshToken());

        //入参（无）
        Map<String, Object> params = new HashMap<>();

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/x-www-form-urlencoded");
        headerMap.put("accept", "application/json");

        //发起POST请求
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok刷新token失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok刷新token失败，返回值 responseMap={}",
                    baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        PlatformTikTokTokenDTO tikTokTokenDTO = null;
        try {
            tikTokTokenDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), PlatformTikTokTokenDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }

        if (StringUtil.isBlank(tikTokTokenDTO.getData().getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.TIK_TOK.getName(), JSONUtil.toJsonStr(apiResult));
        }

        //返回token实体
        return tikTokTokenDTO.getData();
    }
}
