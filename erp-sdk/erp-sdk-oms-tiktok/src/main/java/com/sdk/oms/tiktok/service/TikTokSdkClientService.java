package com.sdk.oms.tiktok.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.common.core.utils.ObjectUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.OrderSplitPramDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.dto.SplittableGroupsBean;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.channel.delivery.DeliveryOptionsBean;
import com.sdk.oms.tiktok.dto.tiktok.channel.delivery.DeliveryOptionsDTO;
import com.sdk.oms.tiktok.dto.tiktok.channel.provider.ShippingProviderDTO;
import com.sdk.oms.tiktok.dto.tiktok.channel.warehouses.WarehousesBean;
import com.sdk.oms.tiktok.dto.tiktok.channel.warehouses.WarehousesDTO;
import com.sdk.oms.tiktok.dto.tiktok.listing.ListingDTO;
import com.sdk.oms.tiktok.dto.tiktok.listing.view.DataBean;
import com.sdk.oms.tiktok.dto.tiktok.listing.view.ListingViewDTO;
import com.sdk.oms.tiktok.dto.tiktok.listing.view.SkusBean;
import com.sdk.oms.tiktok.dto.tiktok.order.OrderDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrderViewDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrdersBean;
import com.sdk.oms.tiktok.dto.tiktok.packages.PackageDetailDTO;
import com.sdk.oms.tiktok.dto.tiktok.packages.PackageDocumentDTO;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOther;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOtherParam;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUS;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUSParam;
import com.sdk.oms.tiktok.dto.tiktok.shop.ShopsBean;
import com.sdk.oms.tiktok.dto.tiktok.shop.TikTokShopAuthDTO;
import com.sdk.oms.tiktok.dto.tiktok.split.PlatformSplitViewDTO;
import com.sdk.oms.tiktok.dto.tiktok.split.SplitAttributesDTO;
import com.sdk.oms.tiktok.dto.tiktok.token.PlatformTikTokTokenDTO;
import com.sdk.oms.tiktok.dto.tiktok.token.TokenDTO;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 美客多平台SDK
 *
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
//    public static void main(String[] args) {
//
//        //标记发货
//        TikTokSdkClientService sdkClientService = new TikTokSdkClientService();
//        TikTokShopInfoDTO tikTokShopInfoDTO = new TikTokShopInfoDTO();
//        tikTokShopInfoDTO.setClientSecret("8ff628de24faf70c24855de4d967fb6a17a47e3f");
//        tikTokShopInfoDTO.setClientId("6buinkjt3hmld");
//        tikTokShopInfoDTO.setAccessToken("ROW_78TplgAAAACj-JAAAriAWjVtF2MrUIFdiwpvtmvHXedAYA9cevCkZepCOiMyd4q0eyFfSnzeQNSPiYsbBmXgfkz3-MVFEcyD6QqmkIhMBjdTRnBo-Bw7DBh-IQ3fvUL_tZGR6DNSekM6IpAJl62sJ5g-cybpZ0kx7WxSxGZu6gHpcqOOBRmzGg");
//        tikTokShopInfoDTO.setShopCipher("TTP_pEhpJwAAAADvOkDJ2jIoaS9Uak191t0d");
//
//        ShipOrderUSParam param = new ShipOrderUSParam();
//        param.setTrackingNumber("9214490357610601379030");
//        param.setShippingProviderId("7117858858072016686");
//        param.setOrderLineItemIds(Arrays.asList("576778267501892253"));
//        sdkClientService.sendTikTokShipOrderUS(tikTokShopInfoDTO, "576649523604197419", param);
//
//        //产品信息查询
//        TikTokSdkClientService sdkClientService = new TikTokSdkClientService();
//        ShopDTO.RefreshTokenDTO refreshTokenDTO = new ShopDTO.RefreshTokenDTO();
//        refreshTokenDTO.setClientId("6buinkjt3hmld");
//        refreshTokenDTO.setClientSecret("8ff628de24faf70c24855de4d967fb6a17a47e3f");
//        refreshTokenDTO.setBaseUrl("https://auth.tiktok-shops.com");
//        refreshTokenDTO.setRefreshToken("GCP_fDcuUQAAAAA5TUxbGIHpDi7CcEx_KnxtwJ4uHOeRIR8CgCHISLDgSCl0GD1hB5Xp_K0ewqhKrl8");
//
//
//        //组装授权url
//        String path = "/api/v2/token/refresh?app_key=%s&app_secret=%s&refresh_token=%s&grant_type=refresh_token";
//        //https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
//        String baseUrl = String.format(refreshTokenDTO.getBaseUrl() + path, refreshTokenDTO.getClientId(), refreshTokenDTO.getClientSecret(), refreshTokenDTO.getRefreshToken());
//
//        //入参（无）
//        Map<String, Object> params = new HashMap<>();
//
//        //请求头
//        Map<String, String> headerMap = new HashMap<>();
//        headerMap.put("accept", "application/json");
//
//        //发起POST请求
//        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
//        if (!Objects.equals(apiResult.getCode(), 200)) {
//            log.error("调用url={},入参params={}, TikTok刷新token失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
//            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok刷新token失败，返回值 responseMap={}",
//                    baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
//        }
//        //解析数据
//        PlatformTikTokTokenDTO tikTokTokenDTO = null;
//        try {
//            tikTokTokenDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), PlatformTikTokTokenDTO.class);
//        } catch (Exception e) {
//            throw new RuntimeException(StrUtil.format("返回值 responseMap={}，转换成实体错误", apiResult.getData()));
//        }
//
//        if (StringUtil.isBlank(tikTokTokenDTO.getData().getAccessToken())) {
//            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.TIK_TOK.getName(), JSONUtil.toJsonStr(apiResult));
//        }

//        paramMap.put("clientId","6buinkjt3hmld");
//        paramMap.put("clientSecret","8ff628de24faf70c24855de4d967fb6a17a47e3f");
//        String acc = "ROW_U5iaMgAAAACj-JAAAriAWjVtF2MrUIFdi7m7AlAtQ6bl6gtVzPM0tTp3IflyK3ZHQk8ZtOa7XO9zFLKnlImAtqdJwcUXfb-yZzbQT5nsA9mvY0FRDmO3S2efX1Q1QLvHUCCS0TfBXkYHYtiTcIi6QO-VPRM69RI9ZSXvMk8rZ1cSj5IJfOA5Xw";
//
//        TikTokShopInfoDTO shopInfoDTO = new TikTokShopInfoDTO();
//        shopInfoDTO.setAccessToken(acc);
//        shopInfoDTO.setShopCipher("TTP_gBJmlwAAAAC67DSXGtKhZV4epbjJVKXF");
//        shopInfoDTO.setClientSecret("8ff628de24faf70c24855de4d967fb6a17a47e3f");
//        shopInfoDTO.setClientId("6buinkjt3hmld");
//        shopInfoDTO.setBaseUrl("https://auth.tiktok-shops.com");
//
//        JobTaskDTO task = new JobTaskDTO();
//        task.setLastTime(LocalDateTime.now().minusDays(3));
//        task.setNextTime(LocalDateTime.now().minusHours(3));
//
//        List<ListingViewDTO> listingViewDTOS = sdkClientService.sendMercadoGetListing(shopInfoDTO);
//        for (ListingViewDTO ordersBean : listingViewDTOS) {
//            System.out.println(ordersBean);
//        }*/
//
//        /*//订单查询
//        //每次最多获取200条
//        Integer pageSize = 100;
//        //分页token
//        String pageToken = "";
//        //平台接口地址
//        String url = TikTokConstant.URL;
//        //服务密钥
//        String secret = "8ff628de24faf70c24855de4d967fb6a17a47e3f";
//        String token = "ROW_U5iaMgAAAACj-JAAAriAWjVtF2MrUIFdi7m7AlAtQ6bl6gtVzPM0tTp3IflyK3ZHQk8ZtOa7XO9zFLKnlImAtqdJwcUXfb-yZzbQT5nsA9mvY0FRDmO3S2efX1Q1QLvHUCCS0TfBXkYHYtiTcIi6QO-VPRM69RI9ZSXvMk8rZ1cSj5IJfOA5Xw";
//
//        while (true) {
//            StringBuffer sb = new StringBuffer();
//            //组装授权url
//            String path = "/order/{version}/orders/search".replace("{version}", TikTokConstant.VERSION);
//            // 定义查询参数
//            Map<String, Object> params = new HashMap<>();
//            params.put("access_token", token);
//            params.put("app_key", "6buinkjt3hmld");
//            params.put("page_size", pageSize);
//            params.put("page_token", pageToken);
//            params.put("shop_cipher", "TTP_pEhpJwAAAADvOkDJ2jIoaS9Uak191t0d");
//            params.put("shop_id", "");
//            params.put("sign", "");
//            String timestamp = System.currentTimeMillis() / 1000 + "";
//            params.put("timestamp", timestamp);
//            params.put("version", TikTokConstant.VERSION);
//
//            //设置请求头
//            Map<String, String> headerMap = new HashMap<>();
//            headerMap.put("content-type", "application/json");
//            headerMap.put("x-tts-access-token", token);
//
//            //请求body，平台用于计算签名
//            Map<String, Object> bodyMap = new HashMap<>();
//            bodyMap.put("update_time_ge", 1714492800L);
//            bodyMap.put("update_time_lt", 1717214400L);
//
//            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
//            // 追加请求路径获取签名
//            String sign = EncryptionUtils.generateSHA256(input, secret);
//            //加入sign签名入参
//            params.put("sign", sign);
//            //组装url
//            sb.append(url);
//            sb.append(path);
//            sb.append("?access_token=" + token + "");
//            sb.append("&app_key=" + "6buinkjt3hmld" + "");
//            sb.append("&page_size=" + pageSize + "");
//            sb.append("&page_token=" + pageToken + "");
//            sb.append("&shop_cipher=" + "TTP_pEhpJwAAAADvOkDJ2jIoaS9Uak191t0d" + "");
//            sb.append("&shop_id=");
//            sb.append("&sign=" + sign + "");
//            sb.append("&timestamp=" + timestamp + "");
//            sb.append("&version=" + TikTokConstant.VERSION + "");
//
//            //拉取数据
//            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
//            if (!Objects.equals(apiResult.getCode(), 200)) {
//                log.error("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
//                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}",
//                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
//            }
//
//            //解析数据
//            ObjectMapper objectMapper = new ObjectMapper();
//            OrderDTO orderDTO = null;
//            try {
//                orderDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
//            } catch (JsonProcessingException e) {
//                log.error("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
//                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}",
//                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
//            }
//
//            if (CollectionUtil.isEmpty(orderDTO.getData().getOrders())) {
//                break;
//            }
//            if (StringUtil.isBlank(orderDTO.getData().getNextPageToken())) {
//                break;
//            }
//            pageToken = orderDTO.getData().getNextPageToken();
//        }*/
//
//    }

    /**
     * 获取token
     *
     * @param paramMap
     * @return
     */
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
        headerMap.put("content-type", "application/json");

        //发起POST请求
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
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

        if (!"success".equalsIgnoreCase(tikTokTokenDTO.getMessage())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.TIK_TOK.getName(), JSONUtil.toJsonStr(apiResult));
        }
        TokenDTO tokenDTO = tikTokTokenDTO.getData();
        if(StringUtils.isBlank(paramMap.get("isFully"))) {
        	//查询店铺权限
            TikTokShopAuthDTO tikTokShopAuthDTO = getAuthorizedShops(paramMap, tokenDTO.getAccessToken());
            for (ShopsBean shop : tikTokShopAuthDTO.getData().getShops()) {
//                if (tokenDTO.getSellerBaseRegion().equalsIgnoreCase(shop.getRegion())) {
                tokenDTO.setShopCipher(shop.getCipher());
                tokenDTO.setShopsBean(shop);
//                }
            }
        }
        tokenDTO.setCode(authCode);
        //返回token实体
        return tokenDTO;
    }


    /**
     * 查询店铺权限
     *
     * @param paramMap
     * @param accessToken
     * @return
     */
    public TikTokShopAuthDTO getAuthorizedShops(Map<String, String> paramMap, String accessToken) {
        String url = TikTokConstant.URL;
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

/*        StringBuffer sb = new StringBuffer();
        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?app_key=" + params.get("app_key") + "");
        sb.append("&sign=" + params.get("sign") + "");
        sb.append("&timestamp=" + params.get("timestamp") + "");
        sb.append("&version=" + params.get("version") + "");*/

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


    /**
     * 刷新token
     *
     * @param refreshTokenDTO
     * @return
     */
    public TokenDTO refreshToken(ShopDTO.RefreshTokenDTO refreshTokenDTO) {

        //组装授权url
        String path = "/api/v2/token/refresh?app_key=%s&app_secret=%s&refresh_token=%s&grant_type=refresh_token";
        //https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
        String baseUrl = String.format(refreshTokenDTO.getBaseUrl() + path, refreshTokenDTO.getClientId(), refreshTokenDTO.getClientSecret(), refreshTokenDTO.getRefreshToken());

        //入参（无）
        Map<String, Object> params = new HashMap<>();

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("accept", "application/json");

        //发起POST请求
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
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
        if (StringUtil.isBlank(shopId)) {
            log.error("===============>TikTok查询店铺授权信息失败，店铺id：{}", shopId);
            return null;
        }
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
            AppClientEnum appClientEnum = AppClientEnum.TIKTOK_ACCESS_TOKEN;
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
            result.setSite(extendData.get("region") + "");
            result.setSellerType(extendData.get("sellerType") + "");
            if (Objects.nonNull(shopAuthEntity)) {
                result.setAccessToken(shopAuthEntity.getAccessToken());
                redisUtil.set(tokenKey, result, shopAuthEntity.getExpiresIn());
            }else{
                throw new ServiceException("店铺未授权");
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

        while (true) {
            StringBuffer sb = new StringBuffer();
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


            pageToken = listingDTO.getData().getNextPageToken();
            //获取到所有客户的产品id
            List<String> productIds = listingDTO.getData().getProducts().stream().map(req -> req.getFid()).distinct().collect(Collectors.toList());

            //根据产品id查询产品详情信息
            List<ListingViewDTO> listingViewDTOS = this.listItemView(productIds, shopInfoDTO);
            resultsBeanList.addAll(listingViewDTOS);

            if (StringUtil.isBlank(listingDTO.getData().getNextPageToken())) {
                break;
            }
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

            DataBean dataBean = listingViewDTO.getData();

            List<SkusBean> skusBeanList = dataBean.getSkus();

            for (SkusBean skus : skusBeanList) {

                DataBean beanCopy = new DataBean();
                BeanUtils.copyProperties(dataBean, beanCopy);

                beanCopy.setSkus(Collections.singletonList(skus));

                ListingViewDTO dto = new ListingViewDTO();
                dto.setCode(listingViewDTO.getCode());
                dto.setData(beanCopy);
                dto.setMessage(listingViewDTO.getMessage());
                dto.setRequestId(listingViewDTO.getRequestId());
                resultList.add(dto);
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
    public List<OrdersBean> sendTikTokGetOrder(TikTokShopInfoDTO shopInfoDTO, JobTaskDTO task) {

        List<OrdersBean> resultsBeanList = new ArrayList<>();

        //每次最多获取200条
        Integer pageSize = 100;
        //分页token
        String pageToken = "";
        //平台接口地址
        String url = TikTokConstant.URL;
        //服务密钥
        String secret = shopInfoDTO.getClientSecret();

        while (true) {
            StringBuffer sb = new StringBuffer();
            //组装授权url
            String path = "/order/" + TikTokConstant.VERSION + "/orders/search";

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
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("update_time_ge", task.getLastTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli() / 1000);
            bodyMap.put("update_time_lt", task.getNextTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli() / 1000);

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

            pageToken = orderDTO.getData().getNextPageToken();
            if (CollectionUtil.isEmpty(orderDTO.getData().getOrders())) {
                continue;
            }
            //获取到所有客户的产品id
            List<String> orderIds = orderDTO.getData().getOrders().stream()
                    .filter(req -> !"UNPAID".equalsIgnoreCase(req.getStatus())
                            && !"ON_HOLD".equalsIgnoreCase(req.getStatus())
                    ).map(req -> req.getFid()).distinct().collect(Collectors.toList());

            //根据订单id查询订单详情信息
            List<OrdersBean> orderViewDTOS = this.listOrderView(orderIds, shopInfoDTO);
            resultsBeanList.addAll(orderViewDTOS);

            if (StringUtil.isBlank(orderDTO.getData().getNextPageToken())) {
                break;
            }
        }

        if (CollectionUtils.isEmpty(resultsBeanList)) {
            return Collections.emptyList();
        }
        return resultsBeanList;
    }

    /**
     * 拉取平台订单详情
     *
     * @param orderIds
     * @param shopInfoDTO
     * @return
     */
    private List<OrdersBean> listOrderView(List<String> orderIds, TikTokShopInfoDTO shopInfoDTO) {
        List<List<String>> partition = Lists.partition(orderIds, 50);
        //平台接口地址
        String url = TikTokConstant.URL;

        //组装授权url
        String path = "/order/" + TikTokConstant.VERSION + "/orders";

        //服务密钥
        String secret = shopInfoDTO.getClientSecret();

        List<OrdersBean> resultsBeanList = new ArrayList<>();

        for (List<String> list : partition) {

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("ids", StringUtil.join(list, ","));
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "multipart/form-data");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, "");
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, secret);
            //加入sign签名入参
            params.put("sign", sign);

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询订单详情数据失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单详情数据失败，返回值 responseMap={}",
                        url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            OrderViewDTO orderViewDTO = null;
            try {
                orderViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderViewDTO.class);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("调用url={},入参params={}, TikTok订单详情数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单详情数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if (CollectionUtil.isEmpty(orderViewDTO.getData().getOrders())) {
                break;
            }
            resultsBeanList.addAll(orderViewDTO.getData().getOrders());

        }
        return resultsBeanList;
    }

    /**
     * 查询平台是否允许拆分订单
     *
     * @param tikTokShopInfoDTO
     * @param orderId
     * @return
     */
    public SplitAttributesDTO sendTikTokSplitAttributes(TikTokShopInfoDTO tikTokShopInfoDTO, String orderId) {
        String url = TikTokConstant.URL;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/orders/split_attributes";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("order_ids", orderId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "multipart/form-data");

        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");
        // 追加请求路径
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);
        //加入sign入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询订单是否允许拆分失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单是否允许拆分失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        SplitAttributesDTO splitAttributesDTO = null;
        try {
            splitAttributesDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), SplitAttributesDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单是否允许拆分失败返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return splitAttributesDTO;
    }

    /**
     * TikTok订单拆分
     *
     * @param shopId  店铺信息
     * @param orderId 平台订单id
     * @param pramDTO 平台要求的body入参
     */
    public PlatformSplitViewDTO sendTikTokOrdersSplit(String shopId, String orderId, OrderSplitPramDTO pramDTO) {
        TikTokShopInfoDTO tikTokShopInfoDTO = this.getShopInfoByShopId(shopId);

        String url = TikTokConstant.URL;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/orders/" + orderId + "/split";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "application/json");

        //请求body，平台用于计算签名
        Map<String, Object> bodyMap = new HashMap<>();
        List<Object> objectList = new ArrayList<>();
        for (SplittableGroupsBean splittableGroup : pramDTO.getSplittableGroups()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", splittableGroup.getId());
            map.put("order_line_item_ids", splittableGroup.getOrderLineItemIds());
            objectList.add(map);
        }
        bodyMap.put("splittable_groups", objectList);
        String body = JSONUtil.toJsonStr(bodyMap);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, body);

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + tikTokShopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + tikTokShopInfoDTO.getClientId() + "");
        sb.append("&shop_cipher=" + tikTokShopInfoDTO.getShopCipher() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");
        sb.append("&version=" + TikTokConstant.VERSION + "");

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), body, null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok订单拆分失败，返回值 responseMap={}", sb.toString(), body, JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单拆分失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        PlatformSplitViewDTO platformSplitViewDTO = null;
        try {
            platformSplitViewDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), PlatformSplitViewDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单拆分返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return platformSplitViewDTO;
    }

    /**
     * 查询平台发货渠道
     *
     * @param shopId 店铺信息
     */
    public List<ShippingProviderDTO> sendTikTokLogisticsChannel(String shopId) {
        TikTokShopInfoDTO tikTokShopInfoDTO = this.getShopInfoByShopId(shopId);

        String url = TikTokConstant.URL;
        String path = "/logistics/" + TikTokConstant.VERSION + "/warehouses";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "multipart/form-data");

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询平台发货渠道失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单拆分失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        WarehousesDTO warehousesDTO = null;
        try {
            warehousesDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), WarehousesDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询平台发货渠道返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }

        List<ShippingProviderDTO> providerDTOS = new ArrayList<>();
        for (WarehousesBean warehouse : warehousesDTO.getData().getWarehouses()) {
            //根据平台仓库id查询发货选项
            DeliveryOptionsDTO deliveryOptionsDTO = this.sendTikTokDeliveryOptions(tikTokShopInfoDTO, warehouse.getId());
            for (DeliveryOptionsBean deliveryOption : deliveryOptionsDTO.getData().getDeliveryOptions()) {
                //根据发货选项查询物流渠道
                ShippingProviderDTO shippingProviderDTO = this.sendTikTokShippingProviders(tikTokShopInfoDTO, deliveryOption.getId());
                providerDTOS.add(shippingProviderDTO);
            }
        }
        return providerDTOS;
    }

    /**
     * 根据平台仓库id查询发货选项
     *
     * @param tikTokShopInfoDTO 店铺信息
     * @param warehouseId       平台仓库id
     */
    public DeliveryOptionsDTO sendTikTokDeliveryOptions(TikTokShopInfoDTO tikTokShopInfoDTO, String warehouseId) {

        String url = TikTokConstant.URL;
        String path = "/logistics/" + TikTokConstant.VERSION + "/warehouses/" + warehouseId + "/delivery_options";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "multipart/form-data");

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询发货选项失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询发货选项失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        DeliveryOptionsDTO deliveryOptionsDTO = null;
        try {
            deliveryOptionsDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), DeliveryOptionsDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询发货选项返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return deliveryOptionsDTO;
    }

    /**
     * 根据发货选项查询物流渠道
     *
     * @param tikTokShopInfoDTO 店铺信息
     * @param deliveryOptionId  平台发货选项id
     */
    public ShippingProviderDTO sendTikTokShippingProviders(TikTokShopInfoDTO tikTokShopInfoDTO, String deliveryOptionId) {
        String url = TikTokConstant.URL;
        String path = "/logistics/" + TikTokConstant.VERSION + "/delivery_options/" + deliveryOptionId + "/shipping_providers";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "multipart/form-data");

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok物流渠道失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok物流渠道失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        ShippingProviderDTO shippingProviderDTO = null;
        try {
            shippingProviderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), ShippingProviderDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok物流渠道返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return shippingProviderDTO;
    }

    /**
     * 订单发货（美国站）
     *
     * @param tikTokShopInfoDTO 店铺信息
     * @param orderId           平台订单id
     */
    public ShipOrderUS sendTikTokShipOrderUS(TikTokShopInfoDTO tikTokShopInfoDTO, String orderId, ShipOrderUSParam paramDTO) {
        String url = TikTokConstant.URL;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/orders/" + orderId + "/packages";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "application/json");

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        String bodyJson = gson.toJson(paramDTO);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, bodyJson);

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + tikTokShopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + tikTokShopInfoDTO.getClientId() + "");
        sb.append("&shop_cipher=" + tikTokShopInfoDTO.getShopCipher() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");
        sb.append("&version=" + TikTokConstant.VERSION + "");
        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), bodyJson, null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok订单发货（美国站）失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单发货（美国站）失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        ShipOrderUS shipOrderUS = null;
        try {
            shipOrderUS = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), ShipOrderUS.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单发货（美国站）返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return shipOrderUS;
    }

    public PackageDocumentDTO getPackageDocument(TikTokShopInfoDTO tikTokShopInfoDTO, String packageId,String type) {
        String url = TikTokConstant.URL;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/packages/" + packageId +"/shipping_documents";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        String shopCipher = tikTokShopInfoDTO.getShopCipher();
        String token = tikTokShopInfoDTO.getAccessToken();
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", token);
        params.put("document_type", type);
        params.put("app_key", clientId);
        params.put("shop_cipher", shopCipher);
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", token);
        headerMap.put("content-type", "application/json");

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询包裹明细失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询包裹明细失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        PackageDocumentDTO result = null;
        try {
            result = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), PackageDocumentDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, TikTok查询包裹明细（美国站）返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }

        return result;
    }

    public PackageDetailDTO getPackageDetail(TikTokShopInfoDTO tikTokShopInfoDTO, String packageId) {
        String url = TikTokConstant.URL;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/packages/" + packageId ;
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        String shopCipher = tikTokShopInfoDTO.getShopCipher();
        String token = tikTokShopInfoDTO.getAccessToken();
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", token);
        params.put("app_key", clientId);
        params.put("shop_cipher", shopCipher);
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", token);
        headerMap.put("content-type", "application/json");

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询包裹明细失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询包裹明细失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        PackageDetailDTO result = null;
        try {
            result = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), PackageDetailDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, TikTok查询包裹明细（美国站）返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }

        return result;
    }

    /**
     * 订单发货（非美国站点）
     * * Use Ship Package for local and cross-border sellers in the ID, SG, TH, PH, VN, MY, UK markets
     *
     * @param tikTokShopInfoDTO 店铺信息
     * @param packageId         平台包裹号
     */
    public ShipOrderOther sendTikTokShipOrderOther(TikTokShopInfoDTO tikTokShopInfoDTO, String packageId, ShipOrderOtherParam paramDTO) {
        String url = TikTokConstant.URL;

        String path = "/fulfillment/" + TikTokConstant.VERSION + "/packages/" + packageId + "/ship";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "application/json");

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        String bodyJson = gson.toJson(paramDTO);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, bodyJson);

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + tikTokShopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + tikTokShopInfoDTO.getClientId() + "");
        sb.append("&shop_cipher=" + tikTokShopInfoDTO.getShopCipher() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");
        sb.append("&version=" + TikTokConstant.VERSION + "");

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), bodyJson, null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok订单发货（其他站点）失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单发货（非美国站点）失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        ShipOrderOther shipOrderOther = null;
        try {
            shipOrderOther = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), ShipOrderOther.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单发货（非美国站点）返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return shipOrderOther;
    }
}
