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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 美客多平台SDK
 * @Author Luo_WG
 * @Date 2024/2/27 18:04
 **/
@Slf4j
@Component
public class TikTokSdkClientService {
    public static void main(String[] args) {
        //a337ecae375358bb96000c02af13f5a154e499d311e657b990a5fa9faf31436b
        test ();
    }

    public static void test () {
        String path = "/authorization/202309/shops";

        // 定义查询参数
        Map<String, String> params = new HashMap<>();

        params.put("access_token", "ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ");
        params.put("app_key", "6buinkjt3hmld");
        params.put("shop_id", "");
        params.put("sign", "");
        //获取时间戳
        params.put("timestamp", "1712471507");
        params.put("version", "202309");

//https://open-api.tiktokglobalshop.com/authorization/202309/shops?app_key=123abc&sign=5361235029d141222525e303d742f9e38aea052d10896d3197ab9d6233730b8c&timestamp=1625484268

//https://open-api.tiktokglobalshop.com/api/shop/get_authorized_shop?app_key=123abc&access_token=aaaaa000-aaa0-aaa0-aaa0-aaaaaaaaa000&sign=5361235029d141222525e303d742f9e38aea052d10896d3197ab9d6233730b8c&timestamp=1625484268&shop_id=36123502970007




        String secret = "8ff628de24faf70c24855de4d967fb6a17a47e3f";

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("content-type", "application/json");

        String input = urlParamsSort(params, path, headerMap, secret);
        // 追加请求路径
        String sign = generateSHA256(input, secret);
        System.out.println("追加请求路径后：" + input);

        params.put("sign", sign);

        String url = "https://open-api.tiktokglobalshop.com";

        //拉取数据
        ApiResult orderResult = HttpCommonUtil.sendOkHttpApiResult(url+path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(orderResult.getCode(), 200) && !Objects.equals(orderResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多marketplace/orders数据失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(orderResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                    url+path, headerMap.toString(), JSONUtil.toJsonStr(orderResult)));
        }
    }
    private static void urlSplicing(StringBuilder input, String path, Map<String, String> header, String paramStr) {
        input.insert(0, path);

        // 如果请求标头 content_type 不是 multipart/form-data，则追加到末尾 body
        if (!"multipart/form-data".equals(header.get("content_type"))) {
            input.append(paramStr);
        }
    }

    /**
     * 第一步提取除sign和access_token之外的所有查询参数。按字母顺序对参数的键重新排序
     * @param queries
     * @return
     */
    private static String urlParamsSort(Map<String, String> queries, String path, Map<String, String> headerMap, String secret) {
        // 提取除 "sign" 和 "access_token" 之外的所有查询参数
        List<String> keys = new ArrayList<>();
        for (String k : queries.keySet()) {
            if (!"sign".equals(k) && !"access_token".equals(k)) {
                keys.add(k);
            }
        }

        // 按字母顺序对参数的键重新排序
        Collections.sort(keys);

        // 生成重新排序的查询键字符串
        StringBuilder input = new StringBuilder();
        for (String key : keys) {
            input.append(key).append(queries.get(key));
        }

        input.insert(0, path);
        /*// 如果请求标头 content_type 不是 multipart/form-data，则追加到末尾 body
        if (!"multipart/form-data".equals(headerMap.get("content_type"))) {
            input.append(JSONUtil.toJsonStr(queries));
        }*/
        String finalString = secret + input.toString() + secret;
        return finalString;
    }

    private static String generateSHA256(String input, String secret) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            hmacSha256.init(secretKey);
            byte[] hash = hmacSha256.doFinal(input.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace(); // 处理异常
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
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
        //https://auth.tiktok-shops.com/api/v2/token/get?app_key=6buinkjt3hmld&auth_code=ROW_fMForwAAAAAnHxxLkmbK6_5EjpV5Fe3TFAZztGCCUNTYS9Ncj5BHOEZr45OmjjjgKLuvsB12opx9yJvUgHadndBbqdRQZes-8GF5gXG22OX5vk6rG_cw1sv8BuS8L7zDkwyjNKQ0ps-9yp0daANK5GtmJ5h7CqVG3MEVumPxZ3FJqzufIygTCg&app_secret=8ff628de24faf70c24855de4d967fb6a17a47e3f&grant_type=authorized_code
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
