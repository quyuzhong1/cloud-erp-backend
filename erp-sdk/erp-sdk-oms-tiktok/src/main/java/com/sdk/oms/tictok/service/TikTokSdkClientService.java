package com.sdk.oms.tictok.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sdk.oms.tictok.constant.TikTokConstant;
import com.sdk.oms.tictok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tictok.dto.tiktok.listing.ListingDTO;
import com.sdk.oms.tictok.dto.tiktok.listing.view.ListingViewDTO;
import com.sdk.oms.tictok.dto.tiktok.listing.view.SkusBean;
import com.sdk.oms.tictok.dto.tiktok.order.OrderDTO;
import com.sdk.oms.tictok.dto.tiktok.order.view.OrderViewDTO;
import com.sdk.oms.tictok.dto.tiktok.shop.ShopsBean;
import com.sdk.oms.tictok.dto.tiktok.shop.TikTokShopAuthDTO;
import com.sdk.oms.tictok.dto.tiktok.token.PlatformTikTokTokenDTO;
import com.sdk.oms.tictok.dto.tiktok.token.TokenDTO;
import com.sdk.oms.tictok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.ZoneOffset;
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
        String baseUrl = String.format(url + path, clientId, authCode, clientSecret);

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
        TokenDTO tokenDTO = tikTokTokenDTO.getData();
        //查询店铺权限
        TikTokShopAuthDTO tikTokShopAuthDTO = getAuthorizedShops(paramMap, tokenDTO.getAccessToken());
        for (ShopsBean shop : tikTokShopAuthDTO.getData().getShops()) {
            if (tokenDTO.getSellerBaseRegion().equalsIgnoreCase(shop.getRegion())) {
                tokenDTO.setShopCipher(shop.getCipher());
            }
        }
        //返回token实体
        return tokenDTO;
    }


    public TikTokShopAuthDTO getAuthorizedShops(Map<String, String> paramMap, String accessToken) {
        String url = paramMap.get("baseUrl");
        String path = "/authorization/" + TikTokConstant.VERSION + "/shops";
        String clientSecret = paramMap.get("clientSecret");
        String clientId = paramMap.get("clientId");
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("app_key", clientId);
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", accessToken);
        headerMap.put("content-type", "multipart/form-data");

        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");
        // 追加请求路径
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);
        //加入sign入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询店铺权限失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询店铺权限失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        TikTokShopAuthDTO tikTokTokenDTO = null;
        try {
            tikTokTokenDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), TikTokShopAuthDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询店铺权限返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return tikTokTokenDTO;
    }


    public TokenDTO refreshToken(ShopDTO.RefreshTokenDTO refreshTokenDTO) {

        //组装授权url
        String path = "/api/v2/token/refresh?app_key=%s&app_secret=%s&refresh_token=%s&grant_type=refresh_token";
        //https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
        String baseUrl = String.format(refreshTokenDTO.getBaseUrl() + path, refreshTokenDTO.getClientId(), refreshTokenDTO.getClientSecret(), refreshTokenDTO.getRefreshToken());

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
     *
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
            result.setShopCipher(extendData.get("shopCipher") + "");
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
     *
     * @param shopInfoDTO
     * @return
     */
    public List<ListingViewDTO> sendMercadoGetListing(TikTokShopInfoDTO shopInfoDTO) {

        List<ListingViewDTO> resultsBeanList = new ArrayList<>();

        //每次最多获取200条
        Integer pageSize = 100;
        //分页token
        String pageToken = "";
        //平台接口地址
        String url = TikTokConstant.URL;
        //服务密钥
        String secret = shopInfoDTO.getClientSecret();


        StringBuffer sb = new StringBuffer();
        while (true) {
            //组装授权url
            String path = "/product/" + TikTokConstant.VERSION + "/products/search";

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("page_size", pageSize);
            params.put("page_token", pageToken);
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            params.put("sign", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, String> bodyMap = new HashMap<>();

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, secret);
            //加入sign签名入参
            params.put("sign", sign);

            //组装url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + params.get("access_token") + "");
            sb.append("&app_key=" + params.get("app_key") + "");
            sb.append("&page_size=" + params.get("page_size") + "");
            sb.append("&page_token=" + params.get("page_token") + "");
            sb.append("&shop_cipher=" + params.get("shop_cipher") + "");
            sb.append("&shop_id=" + params.get("shop_id") + "");
            sb.append("&sign=" + params.get("sign") + "");
            sb.append("&timestamp=" + params.get("timestamp") + "");
            sb.append("&version=" + params.get("version") + "");

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }


            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingDTO listingDTO = null;
            try {
                listingDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingDTO.class);
            } catch (JsonProcessingException e) {
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (StringUtil.isBlank(listingDTO.getData().getNextPageToken())) {
                break;
            }
            pageToken = listingDTO.getData().getNextPageToken();
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
     *
     * @param productIds
     * @param shopInfoDTO
     * @return
     */
    private List<ListingViewDTO> listItemView(List<String> productIds, TikTokShopInfoDTO shopInfoDTO) {

        List<ListingViewDTO> resultList = new ArrayList<>();
        String url = TikTokConstant.URL;
        for (String productId : productIds) {
            //组装授权url
            String path = "/product/" + TikTokConstant.VERSION + "/products/" + productId + "";
            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "multipart/form-data");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, shopInfoDTO.getClientSecret(), "");
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, shopInfoDTO.getClientSecret());
            //加入sign签名入参
            params.put("sign", sign);

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
                        url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingViewDTO listingViewDTO = null;
            try {
                listingViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingViewDTO.class);
            } catch (JsonProcessingException e) {
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            for (SkusBean skus : listingViewDTO.getData().getSkus()) {
                listingViewDTO.getData().setSkus(Arrays.asList(skus));
                resultList.add(listingViewDTO);
            }
        }

        return resultList;
    }


    /**
     * 发送请求获取指定店铺的订单信息
     *
     * @param shopInfoDTO
     * @return
     */
    public List<OrderViewDTO> sendTikTokGetOrder(TikTokShopInfoDTO shopInfoDTO, JobTaskDTO task) {

        List<OrderViewDTO> resultsBeanList = new ArrayList<>();

        //每次最多获取200条
        Integer pageSize = 100;
        //分页token
        String pageToken = "";
        //平台接口地址
        String url = TikTokConstant.URL;
        //服务密钥
        String secret = shopInfoDTO.getClientSecret();

        StringBuffer sb = new StringBuffer();
        while (true) {
            //组装授权url
            String path = "order/" + TikTokConstant.VERSION + "/orders/search";

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("page_size", pageSize);
            params.put("page_size", pageToken);
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            params.put("sign", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("update_time_ge", task.getLastTime().toEpochSecond(ZoneOffset.UTC));
            bodyMap.put("update_time_lt", task.getNextTime().toEpochSecond(ZoneOffset.UTC));

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, secret);
            //加入sign签名入参
            params.put("sign", sign);

            //组装url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
            sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
            sb.append("&page_size=" + pageSize + "");
            sb.append("&page_token=" + pageToken + "");
            sb.append("&shop_cipher=" + shopInfoDTO.getShopCipher() + "");
            sb.append("&shop_id=");
            sb.append("&sign=" + sign + "");
            sb.append("&timestamp=" + timestamp + "");
            sb.append("&version=" + TikTokConstant.VERSION + "");

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            OrderDTO orderDTO = null;
            try {
                orderDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            } catch (JsonProcessingException e) {
                log.error("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (StringUtil.isBlank(orderDTO.getData().getNextPageToken())) {
                break;
            }
            pageToken = orderDTO.getData().getNextPageToken();
            //获取到所有客户的产品id
            List<String> orderIds = orderDTO.getData().getOrders().stream().map(req -> req.getFid()).distinct().collect(Collectors.toList());

            //根据订单id查询订单详情信息
            List<OrderViewDTO> orderViewDTOS = this.listOrderView(orderIds, shopInfoDTO);
            resultsBeanList.addAll(orderViewDTOS);
        }

        if (CollectionUtils.isEmpty(resultsBeanList)) {
            return Collections.emptyList();
        }
        return resultsBeanList;
    }

    /**
     * 拉取平台订单详情
     * @param orderIds
     * @param shopInfoDTO
     * @return
     */
    private List<OrderViewDTO> listOrderView(List<String> orderIds, TikTokShopInfoDTO shopInfoDTO) {
        List<List<String>> partition = Lists.partition(orderIds, 50);
        //平台接口地址
        String url = TikTokConstant.URL;

        //组装授权url
        String path = "order/" + TikTokConstant.VERSION + "/orders";

        //服务密钥
        String secret = shopInfoDTO.getClientSecret();

        List<OrderViewDTO> resultsBeanList = new ArrayList<>();

        for (List<String> list : partition) {

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("ids", list);
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, "");
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, secret);
            //加入sign签名入参
            params.put("sign", sign);

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url+path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询订单详情数据失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单详情数据失败，返回值 responseMap={}",
                        url+path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            List<OrderViewDTO> dataList = null;
            try {
                dataList = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), new TypeReference<List<OrderViewDTO>>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("调用url={},入参params={}, TikTok订单详情数据解析失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单详情数据解析失败，返回值 responseMap={}",
                        url+path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if(CollectionUtil.isEmpty(dataList)){
                break;
            }
            resultsBeanList.addAll(dataList);

        }
        return resultsBeanList;
    }
}
