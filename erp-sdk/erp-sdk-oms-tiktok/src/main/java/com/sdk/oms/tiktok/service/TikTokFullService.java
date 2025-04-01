package com.sdk.oms.tiktok.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyDeliveryReq;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyDeliveryResp;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;

/**
 * 全托管
 *
 **/
@Slf4j
@Component
public class TikTokFullService {
    private static RedisUtil redisUtil;


    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        TikTokFullService.redisUtil = redisUtil;
    }

    public static void main(String[] args) throws JsonProcessingException {
        TikTokFullyDeliveryReq tikTokFullyDeliveryReq = new TikTokFullyDeliveryReq();
        List<TikTokFullyDeliveryReq.PackagesDTO> packagesDTOList = new ArrayList<>();
        List<TikTokFullyDeliveryReq.PackagesDTO.ItemsDTO> itemsDTOList = new ArrayList<>();
        itemsDTOList.add(new TikTokFullyDeliveryReq.PackagesDTO.ItemsDTO("TESTS24081900014200101", 1));
        TikTokFullyDeliveryReq.PackagesDTO packagesDTO = new TikTokFullyDeliveryReq.PackagesDTO(itemsDTOList);
        packagesDTOList.add(packagesDTO);
        tikTokFullyDeliveryReq.setPackages(packagesDTOList);
        tikTokFullyDeliveryReq.setStockupOrderCode("POCYT2409040000039");
        tikTokFullyDeliveryReq.setPackageQuantity(1);

        String url = TikTokConstant.URL;
        String secret = "530a7d739653179b07d7026a63cde31671f10f61";
        String accessToken = "ROW_B6eCcQAAAABVlKa00W9Nne3pV522i3L6Ie04obpPK2d-5Rq9rL7jO0YSbotwLjpZ4DfljtV--ZI9dIDBdmztWivYPnUtt7dp";
        String appKey = "69p2ui5hr09sn";
        StringBuffer sb = new StringBuffer();
        //组装授权url
        String path = "/gs_full_service_shipment/202405/beta/delivery_orders";

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("app_key", appKey);
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("delivery_order", tikTokFullyDeliveryReq);
        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", accessToken);

        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, secret);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=").append(accessToken);
        sb.append("&app_key=").append(appKey);
        sb.append("&sign=").append(sign);
        sb.append("&timestamp=").append(timestamp);

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管创建送货单失败，返回值 responseMap={}", sb, params, JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyDeliveryResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyDeliveryResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管创建送货单失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        System.out.println(orderDTO);
    }

//    public static void main(String[] args) {
//
//        String url = TikTokConstant.URL;
//        String secret = "530a7d739653179b07d7026a63cde31671f10f61";
//        String accessToken = "ROW_B6eCcQAAAABVlKa00W9Nne3pV522i3L6Ie04obpPK2d-5Rq9rL7jO0YSbotwLjpZ4DfljtV--ZI9dIDBdmztWivYPnUtt7dp";
//        String appKey = "69p2ui5hr09sn";
//        StringBuffer sb = new StringBuffer();
//        //组装授权url
//        String path = "/gs_full_service_shipment/{version}/stockup_orders/search".replace("{version}", TikTokConstant.FULLY_ORDER_VERSION);
//
//        // 定义查询参数
//        Map<String, Object> params = new HashMap<>();
//        params.put("access_token", accessToken);
//        params.put("app_key", appKey);
//        String timestamp = System.currentTimeMillis() / 1000 + "";
//        params.put("timestamp", timestamp);
//
//        //设置请求头
//        Map<String, String> headerMap = new HashMap<>();
//        headerMap.put("content-type", "application/json");
//        headerMap.put("x-tts-access-token", accessToken);
//
//        //请求body，平台用于计算签名
//        Map<String, Object> bodyMap = new HashMap<>();
//        bodyMap.put("page_size", 1);
//        bodyMap.put("page_token", "");
////        bodyMap.put("latest_status_update_ge", dmpInputApiInitRequest.getStartTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli() / 1000);
////        bodyMap.put("latest_status_update_lt", dmpInputApiInitRequest.getEndTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli() / 1000);
//        bodyMap.put("order_types", Collections.singletonList("JIT"));
//        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
//        // 追加请求路径获取签名
//        String sign = EncryptionUtils.generateSHA256(input, secret);
//        //加入sign签名入参
//        params.put("sign", sign);
//
//        //组装url
//        sb.append(url);
//        sb.append(path);
//        sb.append("?access_token=" + accessToken + "");
//        sb.append("&app_key=" + appKey + "");
//        sb.append("&sign=" + sign + "");
//        sb.append("&timestamp=" + timestamp + "");
//
//        //拉取数据
//        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
//        if (!Objects.equals(apiResult.getCode(), 200)) {
//            log.error("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
//            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}",
//                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
//        }
//
//        //解析数据
//        ObjectMapper objectMapper = new ObjectMapper();
//        FullyOrderDTO orderDTO = null;
//        try {
//            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<FullyOrderDTO>() {}.getType());
//        } catch (Exception e) {
//            log.error("调用url={},入参params={}, 查询全托管订单数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
//            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}",
//                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
//        }
//
//        System.out.println(JSONUtil.toJsonStr(orderDTO));
//        if (CollectionUtil.isEmpty(orderDTO.getData().getStockupOrders())) {
//
//        }
//
//    }

//
//    public static void main(String[] args) {
//
//        String url = TikTokConstant.URL;
//        String secret = "530a7d739653179b07d7026a63cde31671f10f61";
//        String accessToken = "ROW_B6eCcQAAAABVlKa00W9Nne3pV522i3L6Ie04obpPK2d-5Rq9rL7jO0YSbotwLjpZ4DfljtV--ZI9dIDBdmztWivYPnUtt7dp";
//        String appKey = "69p2ui5hr09sn";
//        StringBuffer sb = new StringBuffer();
//        //组装授权url
//        String path = "/gs_full_service_commodity/{version}/beta/products/search".replace("{version}", TikTokConstant.FULLY_VERSION);
//
//        // 定义查询参数
//        Map<String, Object> params = new HashMap<>();
//        params.put("access_token", accessToken);
//        params.put("app_key", appKey);
//        String timestamp = System.currentTimeMillis() / 1000 + "";
//        params.put("timestamp", timestamp);
//
//        //设置请求头
//        Map<String, String> headerMap = new HashMap<>();
//        headerMap.put("content-type", "application/json");
//        headerMap.put("x-tts-access-token", accessToken);
//
//        //请求body，平台用于计算签名
//        Map<String, Object> bodyMap = new HashMap<>();
//        bodyMap.put("page_size", 50);
//
//        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
//        // 追加请求路径获取签名
//        String sign = EncryptionUtils.generateSHA256(input, secret);
//        //加入sign签名入参
//        params.put("sign", sign);
//
//        //组装url
//        sb.append(url);
//        sb.append(path);
//        sb.append("?access_token=" + params.get("access_token") + "");
//        sb.append("&app_key=" + params.get("app_key") + "");
//        sb.append("&sign=" + params.get("sign") + "");
//        sb.append("&timestamp=" + params.get("timestamp") + "");
//
//        //拉取数据
//        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
//        if (!Objects.equals(apiResult.getCode(), 200)) {
//            log.error("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
//            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
//                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
//        }
//
//        //解析数据
//        ObjectMapper objectMapper = new ObjectMapper();
//        FullyListingDTO listingDTO = null;
//        try {
//            listingDTO = JSON.parseObject(apiResult.getData(),new TypeReference<FullyListingDTO>() {}.getType());
//        } catch (Exception e) {
//            log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
//            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
//                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
//        }
//
//        if (CollectionUtils.isEmpty(listingDTO.getData().getSpus())) {
//
//        }
//    }

    public TikTokFullyDeliveryResp createDelivery(String shopId,TikTokFullyDeliveryReq tikTokFullyDeliveryReq) {

        TikTokShopInfoDTO tikTokShopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        String secret = tikTokShopInfoDTO.getClientSecret();
        String accessToken = tikTokShopInfoDTO.getAccessToken();
        String appKey = tikTokShopInfoDTO.getClientId();
        StringBuffer sb = new StringBuffer();
        //组装授权url
        String path = "/gs_full_service_shipment/202405/beta/delivery_orders";

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("app_key", appKey);
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("delivery_order", tikTokFullyDeliveryReq);
        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", accessToken);

        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, secret);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=").append(accessToken);
        sb.append("&app_key=").append(appKey);
        sb.append("&sign=").append(sign);
        sb.append("&timestamp=").append(timestamp);

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管创建送货单失败，返回值 responseMap={}", sb, params, JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyDeliveryResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyDeliveryResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管创建送货单失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        return orderDTO;
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
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.TIK_TOK_FULLY.getCode(), shopId);
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
            AppClientEnum appClientEnum = AppClientEnum.TIKTOK_FULLY_ACCESS_TOKEN;
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
}
