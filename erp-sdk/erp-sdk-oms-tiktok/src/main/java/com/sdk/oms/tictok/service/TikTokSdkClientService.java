package com.sdk.oms.tictok.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.tictok.constant.TikTokConstant;
import com.sdk.oms.tictok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tictok.dto.tiktok.listing.ListingDTO;
import com.sdk.oms.tictok.dto.tiktok.listing.view.ListingViewDTO;
import com.sdk.oms.tictok.dto.tiktok.token.PlatformTikTokTokenDTO;
import com.sdk.oms.tictok.dto.tiktok.token.TokenDTO;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

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

        String url = "https://open-api.tiktokglobalshop.com";

        String secret = "8ff628de24faf70c24855de4d967fb6a17a47e3f";

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("app_key", "6buinkjt3hmld");
        params.put("sign", "");
        String timestamp = System.currentTimeMillis()/1000 +"";
        System.out.println(timestamp);
        params.put("timestamp", timestamp);
        params.put("version", "202309");

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", "ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ");

        //请求body,POST请求才有，就是POST的入参
        Map<String, String> bodyMap = new HashMap<>();

        String input = urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
        // 追加请求路径
        String sign = generateSHA256(input, secret);
        System.out.println("追加请求路径后：" + input);
        //加入sign入参
        params.put("sign", sign);
        //拉取数据
        ApiResult orderResult = HttpCommonUtil.sendOkHttpApiResult(url+path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(orderResult.getCode(), 200) && !Objects.equals(orderResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多marketplace/orders数据失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(orderResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                    url+path, headerMap.toString(), JSONUtil.toJsonStr(orderResult)));
        }
    }

    /**
     * 第一步提取除sign和access_token之外的所有查询参数。按字母顺序对参数的键重新排序
     * @param queries
     * @return
     */
    private static String urlParamsSort(Map<String, Object> queries, String path, Map<String, String> headerMap, String secret, String bodyStr) {
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
        // 如果请求标头 content_type 不是 multipart/form-data，则追加到末尾 body
        if (!"multipart/form-data".equals(headerMap.get("content_type"))) {
            input.append(JSONUtil.toJsonStr(bodyStr));
        }
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

    /**
     * 查询店铺信息
     * @param shopId
     * @return
     */
    public TikTokShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.TIK_TOK.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof TikTokShopInfoDTO) {
                return (TikTokShopInfoDTO) tokenObj;
            }
        } else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.MERCADO_ACCESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return null;
            }
            TikTokShopInfoDTO result = new TikTokShopInfoDTO();
            result.setBaseUrl(cfgAppClient.getUrl());
            result.setClientId(cfgAppClient.getClientId());
            result.setClientSecret(cfgAppClient.getClientSecret());
            result.setId(shopId);
            Map<String, Object> extendData = shopInfoEntity.getExtendData();
            result.setShopCipher(extendData.get("shopCipher")+"");
            result.setSite(shopInfoEntity.getDictCountryCode());
            if (Objects.nonNull(shopAuthEntity)) {
                result.setAccessToken(shopAuthEntity.getAccessToken());
                redisUtil.set(tokenKey, result, shopAuthEntity.getExpiresIn());
            }

            return result;
        }
        return null;

    }

    /**
     * 发送请求获取指定店铺的sku信息
     * @param shopInfoDTO
     * @return
     */
    public List<ListingViewDTO> sendMercadoGetListing(TikTokShopInfoDTO shopInfoDTO) {

        List<ListingViewDTO> resultsBeanList = new ArrayList<>();

        //每次最多获取200条
        Integer pageSize = 100;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;
        //平台接口地址
        String url = TikTokConstant.URL;
        //服务密钥
        String secret = shopInfoDTO.getClientSecret();
        while(true) {
            //组装授权url
            String path = "/product/"+ TikTokConstant.VERSION+"/products/search";

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("page_size", pageSize);
            params.put("page_token", pageSize);
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            params.put("sign", "");
            String timestamp = System.currentTimeMillis()/1000 +"";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, String> bodyMap = new HashMap<>();

            String input = urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = generateSHA256(input, secret);
            //加入sign签名入参
            params.put("sign", sign);
            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url+path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getMsg(), "Success")) {
                log.error("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
                        url+path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }


            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingDTO listingDTO = null;
            try {
                listingDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingDTO.class);
            } catch (JsonProcessingException e) {
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url+path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (StringUtil.isBlank(listingDTO.getData().getNextPageToken())) {
                break;
            }
            //获取到所有客户的产品id
            List<String> productIds = listingDTO.getData().getProducts().stream().map(req -> req.getFid()).distinct().collect(Collectors.toList());

            //根据产品id查询产品详情信息
            List<ListingViewDTO> listingViewDTOS = this.listItemView(productIds, shopInfoDTO);
            resultsBeanList.addAll(listingViewDTOS);

        }

        if (CollectionUtils.isEmpty(resultsBeanList)) {
            return Collections.emptyList();
        }

        return resultsBeanList;
    }

    /**
     * 根据产品id查询产品详情信息
     * @param productIds
     * @param shopInfoDTO
     * @return
     */
    private List<ListingViewDTO> listItemView(List<String> productIds, TikTokShopInfoDTO shopInfoDTO) {

        List<ListingViewDTO> resultList = new ArrayList<>();
        String url = TikTokConstant.URL;
        for (String productId : productIds) {
            //组装授权url
            String path = "/product/"+ TikTokConstant.VERSION+"/products/"+ productId+"";
            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            params.put("sign", "");
            String timestamp = System.currentTimeMillis()/1000 +"";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, Object> bodyMap = new HashMap<>();

            String input = urlParamsSort(params, path, headerMap, shopInfoDTO.getClientSecret(), JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = generateSHA256(input, shopInfoDTO.getClientSecret());
            //加入sign签名入参
            params.put("sign", sign);
            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url+path, JSONUtil.toJsonStr(params), bodyMap, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getMsg(), "Success")) {
                log.error("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
                        url+path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingViewDTO listingViewDTO = null;
            try {
                listingViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingViewDTO.class);
            } catch (JsonProcessingException e) {
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url+path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            resultList.add(listingViewDTO);
        }

        return resultList;
    }


}
