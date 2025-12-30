package com.sdk.oms.tiktok.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.URLDecoder;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.fully.*;
import com.sdk.oms.tiktok.dto.tiktok.listing.FullyListingDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyDeliveryOrderDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyOrderDTO;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
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
    public static final String TIMESTAMP = "timestamp";
    public static final String VERSION = "version";

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        TikTokFullService.redisUtil = redisUtil;
    }
//    public static void main(String[] args) {
//        TikTokShopInfoDTO shopInfoDTO = new TikTokShopInfoDTO();
//        shopInfoDTO.setClientId("69p2ui5hr09sn");
//        shopInfoDTO.setClientSecret("530a7d739653179b07d7026a63cde31671f10f61");
//        shopInfoDTO.setAccessToken("ROW_XAfl5gAAAABVlKa00W9Nne3pV522i3L6oO4UF5BTcu6oicbfEMg135EDc0ENdIfBlBgSVt4R-f5_y2kCm_KkNYy-IzYlnKyq");
//        String url = TikTokConstant.URL;
//        StringBuffer sb = new StringBuffer();
//        String path = "/gs_full_service_shipment/202410/shipping_providers/search";
//        String toktikInfo = shopInfoDTO.getClientSecret();
//
//        TikTokFullyShippingProviderReq tikTokFullyShippingProviderReq = new TikTokFullyShippingProviderReq();
//        tikTokFullyShippingProviderReq.setDeliveryMode("SELF_DELIVERY");
//        tikTokFullyShippingProviderReq.setDeliveryOrderCodes(Arrays.asList("POCYT2408210000023S01"));
//        tikTokFullyShippingProviderReq.setSenderContactId("5765611516297017604");
//        tikTokFullyShippingProviderReq.setDeliveryOption("SUPER_SPEEDY_EXPRESS");
//        tikTokFullyShippingProviderReq.setTotalWeight(new TikTokFullyShippingProviderReq.TotalWeightDTO("1240","GRAM"));
//        // 定义查询参数
//        Map<String, Object> params = new HashMap<>();
//        params.put("access_token", shopInfoDTO.getAccessToken());
//        params.put("app_key", shopInfoDTO.getClientId());
//        String timestamp = System.currentTimeMillis() / 1000 + "";
//        params.put("timestamp", timestamp);
//
//        //设置请求头
//        Map<String, String> headerMap = new HashMap<>();
//        headerMap.put("content-type", "application/json");
//        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());
//
//        //请求body，平台用于计算签名
//
//        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(tikTokFullyShippingProviderReq));
//        // 追加请求路径获取签名
//        String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
//        //加入sign签名入参
//        params.put("sign", sign);
//
//        //组装url
//        sb.append(url);
//        sb.append(path);
//        sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
//        sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
//        sb.append("&sign=" + sign + "");
//        sb.append("&timestamp=" + timestamp + "");
//
//        //拉取数据
//        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(tikTokFullyShippingProviderReq), null, headerMap, RequestMethod.POST);
//        if (!Objects.equals(apiResult.getCode(), 200)) {
//            log.error("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
//            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}",
//                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
//        }
//
//        //解析数据
//        TikTokFullyAddressResp orderDTO = null;
//        try {
//            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyAddressResp>() {}.getType());
//        } catch (Exception e) {
//            log.error("调用url={},入参params={}, TikTok全托管打印sku失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
//            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}",
//                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
//        }
//        if(orderDTO.getCode()!=0){
//            throw new ServiceException("TT全托管打印sku失败:{}",orderDTO.getMessage());
//        }
//    }
    public static void main(String[] args) {
        TikTokShopInfoDTO shopInfoDTO = new TikTokShopInfoDTO();
        shopInfoDTO.setClientId("69p2ui5hr09sn");
        shopInfoDTO.setClientSecret("530a7d739653179b07d7026a63cde31671f10f61");
        shopInfoDTO.setAccessToken("ROW_XAfl5gAAAABVlKa00W9Nne3pV522i3L6oO4UF5BTcu6oicbfEMg135EDc0ENdIfBlBgSVt4R-f5_y2kCm_KkNYy-IzYlnKyq");
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_commodity/{version}/beta/products/search".replace("{version}", TikTokConstant.FULLY_VERSION);
        String toktikInfo = shopInfoDTO.getClientSecret();
        String secret = "530a7d739653179b07d7026a63cde31671f10f61";
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body，平台用于计算签名
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("page_size", 50);
        bodyMap.put("platform_spu_codes", Arrays.asList("TESTS241125000359"));
//        bodyMap.put("page_token", "");

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
        sb.append("&sign=" + params.get("sign") + "");
        sb.append("&timestamp=" + params.get("timestamp") + "");

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        ObjectMapper objectMapper = new ObjectMapper();
        FullyListingDTO listingDTO = null;
        try {
            listingDTO = JSON.parseObject(apiResult.getData(),new TypeReference<FullyListingDTO>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }

    }
    /**
     * 打印sku
     * @param tikTokFullyPrintSkuReq
     * @param shopId
     * @return
     */
    public String printSku(TikTokFullyPrintSkuReq tikTokFullyPrintSkuReq,String shopId) {
        String url = TikTokConstant.URL;
        TikTokShopInfoDTO tikTokShopInfoDTO = this.getShopInfoByShopId(shopId);
        String secret = tikTokShopInfoDTO.getClientSecret();
        String accessToken = tikTokShopInfoDTO.getAccessToken();
        String appKey = tikTokShopInfoDTO.getClientId();
        StringBuffer sb = new StringBuffer();
        //组装授权url
        String path = "/gs_full_service_shipment/202503/delivery_orders/sku_documents/generate";

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("app_key", appKey);
        String timestamp = System.currentTimeMillis() / 1000 + "";

        params.put("timestamp", timestamp);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("stockup_order_code",tikTokFullyPrintSkuReq.getStockupOrderCode());
        bodyMap.put("size",tikTokFullyPrintSkuReq.getSize());
        bodyMap.put("platform_sku_items",tikTokFullyPrintSkuReq.getPlatformSkuItems());
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
            log.error("调用url={},入参params={}, TikTok全托管打印sku失败，返回值 responseMap={}", sb, params, JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管打印sku失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyPrintDeliveryResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyPrintDeliveryResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管打印sku失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={},打印sku数据解析失败，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        if(orderDTO.getCode()!=0){
            throw new ServiceException("TT全托管打印sku失败:{}",orderDTO.getMessage());
        }
        if(StringUtils.isNotBlank(orderDTO.getData().getDocumentUrl())){
            String documentUrl = orderDTO.getData().getDocumentUrl();
            String decoded = StringEscapeUtils.unescapeJava(documentUrl)
                    .replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            documentUrl = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
            return documentUrl;
        }else{
            throw new ServiceException("TT全托管打印sku失败:文件路径为空");
        }
    }

    public TikTokFullyPrintDeliveryResp printDelivery(String deliveryCode,String shopId) {
        if(StringUtil.isBlank(deliveryCode)){
            throw new ServiceException("送货单号不能为空");
        }
        TikTokShopInfoDTO tikTokShopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        String secret = tikTokShopInfoDTO.getClientSecret();
        String accessToken = tikTokShopInfoDTO.getAccessToken();
        String appKey = tikTokShopInfoDTO.getClientId();
        StringBuffer sb = new StringBuffer();
        //组装授权url
        String path = "/gs_full_service_shipment/202405/beta/delivery_orders/documents";

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("app_key", appKey);
        String timestamp = System.currentTimeMillis() / 1000 + "";

        params.put("timestamp", timestamp);
        params.put("delivery_order_codes",deliveryCode);
        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", accessToken);

        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, "");
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
        sb.append("&delivery_order_codes=").append(deliveryCode);

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管查询送货单面单失败，返回值 responseMap={}", sb, params, JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询送货单面单数据失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyPrintDeliveryResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyPrintDeliveryResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管查询送货单面单失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={},查询送货单面单解析失败，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        return orderDTO;
    }

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
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管创建送货单失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyDeliveryResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyDeliveryResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管创建送货单失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 全托管创建送货单解析失败，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        return orderDTO;
    }

    public List<FullyOrderDTO.DataDTO.StockupOrdersDTO> listOrderByParam(String shopId, List<String> platformCodes) {
        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        if(platformCodes.isEmpty()){
            throw new ServiceException("平台单号不能为空");
        }
        List<List<String>> platformCodeList = ListUtil.partition(platformCodes, 50);
        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_shipment/{version}/stockup_orders/search".replace("{version}", TikTokConstant.FULLY_ORDER_VERSION);
        String toktikInfo = shopInfoDTO.getClientSecret();
        List<FullyOrderDTO.DataDTO.StockupOrdersDTO> stockupOrdersDTOList = new ArrayList<>();
        for (List<String> platformCode : platformCodeList) {
            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("page_size", 50);
            bodyMap.put("order_types", Collections.singletonList("JIT"));
            bodyMap.put("stockup_order_codes", platformCode);
            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
            //加入sign签名入参
            params.put("sign", sign);

            //组装url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
            sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
            sb.append("&sign=" + sign + "");
            sb.append("&timestamp=" + timestamp + "");

            //拉取数据
            ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok全托管查询订单数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管查询订单数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            FullyOrderDTO orderDTO = null;
            try {
                orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<FullyOrderDTO>() {}.getType());
            } catch (Exception e) {
                log.error("调用url={},入参params={}, 查询全托管订单数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询全托管订单数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            stockupOrdersDTOList.addAll(orderDTO.getData().getStockupOrders());
        }
        return stockupOrdersDTOList;
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

    /**
     * 查询送货单信息
     * @param shopId 店铺id
     * @param deliveryCodes 订单物流表中的code 与回参的code相关联
     * @return
     */
    public List<FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO> listDeliveryOrderByParam(String shopId, List<String> deliveryCodes) {

        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        if(deliveryCodes.isEmpty()){
            throw new ServiceException("送货单号不能为空");
        }
        List<List<String>> deliveryCodeList = ListUtil.partition(deliveryCodes, 50);
        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_shipment/{version}/delivery_orders/search".replace("{version}", TikTokConstant.FULLY_ORDER_VERSION);
        String toktikInfo = shopInfoDTO.getClientSecret();
        List<FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO> stockupOrdersDTOList = new ArrayList<>();
        for (List<String> deliveryCode : deliveryCodeList) {
            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("page_size", 50);
            bodyMap.put("order_types", Collections.singletonList("JIT"));
            bodyMap.put("delivery_order_codes", deliveryCode);
            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
            //加入sign签名入参
            params.put("sign", sign);

            //组装url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
            sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
            sb.append("&sign=" + sign + "");
            sb.append("&timestamp=" + timestamp + "");

            //拉取数据
            ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询送货单数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询送货单数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            FullyDeliveryOrderDTO orderDTO = null;
            try {
                orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<FullyDeliveryOrderDTO>() {}.getType());
            } catch (Exception e) {
                log.error("调用url={},入参params={}, 查询全托管送货单数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询送货单数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if(orderDTO.getCode()!=0){
                throw new ServiceException("TT全托管查询送货单失败:{}",orderDTO.getMessage());
            }
            stockupOrdersDTOList.addAll(orderDTO.getData().getDeliveryOrders());
        }
        return stockupOrdersDTOList;
    }
    /**
     * 查询商家地址
     * @param shopId 店铺id
     * @return
     */
    public List<TikTokFullyAddressResp.DataDTO.AddressesDTO> listAddress(String shopId) {
        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_commodity/202407/supplier_addresses";
        String toktikInfo = shopInfoDTO.getClientSecret();

        String pageToken = "";
        String pageSize = "50";
        List<TikTokFullyAddressResp.DataDTO.AddressesDTO> list = new ArrayList<>();
        while (true) {
            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            if(StringUtil.isNotBlank(pageToken)){
                params.put("page_token", pageToken);
            }
            params.put("page_size", pageSize);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, Object> bodyMap = new HashMap<>();
            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, "");
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
            //加入sign签名入参
            params.put("sign", sign);

            //组装url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
            sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
            sb.append("&sign=" + sign + "");
            sb.append("&timestamp=" + timestamp + "");
            if(StringUtil.isNotBlank(pageToken)){
                sb.append("&page_token=" + pageToken + "");
            }
            sb.append("&page_size=" + pageSize + "");

            //拉取数据
            ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok全托管查询地址数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询地址数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            TikTokFullyAddressResp orderDTO = null;
            try {
                orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyAddressResp>() {}.getType());
            } catch (Exception e) {
                log.error("调用url={},入参params={}, TikTok全托管查询地址数据失败，返回值 responseMap={}", url + path, params, JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询地址数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if(orderDTO.getCode()!=0){
                throw new ServiceException("TT全托管查询地址数据失败:{}",orderDTO.getMessage());
            }

            if (CollectionUtil.isEmpty(orderDTO.getData().getAddresses())) {
                break;
            }
            list.addAll(orderDTO.getData().getAddresses());
            pageToken = orderDTO.getData().getNextPageToken();

            if (StringUtil.isBlank(pageToken)) {
                break;
            }
        }
       return list;
    }


    /**
     * 查询预约物流商和日期
     * @param shopId 店铺id
     * @return
     */
    public TikTokFullyShippingProviderResp searchShippingProvider(String shopId,TikTokFullyShippingProviderReq tikTokFullyShippingProviderReq) {
        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_shipment/202410/shipping_providers/search";
        String toktikInfo = shopInfoDTO.getClientSecret();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body，平台用于计算签名
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(tikTokFullyShippingProviderReq));
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(tikTokFullyShippingProviderReq), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管查询可用物流商与预约日期数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管查询可用物流商与预约日期数据，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyShippingProviderResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyShippingProviderResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管查询可用物流商与预约日期数据，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管查询可用物流商与预约日期数据，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        if(orderDTO.getCode()!=0){
            throw new ServiceException("TikTok全托管查询可用物流商与预约日期数据失败:{}",orderDTO.getMessage());
        }
        return orderDTO;
    }

    /**
     * 预约发货
     * @param shopId 店铺id
     * @return
     */
    public TikTokFullyShippingResp shipment(String shopId,TikTokFullyShippingReq tikTokFullyShippingReq) {
        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_shipment/202410/delivery_orders/reserve_ship";
        String toktikInfo = shopInfoDTO.getClientSecret();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body，平台用于计算签名
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(tikTokFullyShippingReq));
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(tikTokFullyShippingReq), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管查询预约发货失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管预约发货，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyShippingResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyShippingResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管预约发货数据，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管预约发货数据，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        if(orderDTO.getCode()!=0){
            throw new ServiceException("TikTok全托管预约发货失败:{}",orderDTO.getMessage());
        }
        return orderDTO;
    }

    /**
     * 物流单查询
     * @param shopId 店铺id
     * @return
     */
    public TikTokFullyLogisticResp queryLogistics(String shopId,List<String> logisticCodes) {
        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        String logisticCode = String.join(",", logisticCodes);

        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_shipment/202407/logistics_orders";
        String toktikInfo = shopInfoDTO.getClientSecret();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);
        params.put("logistics_orders", logisticCode);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body，平台用于计算签名
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, "");
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");
        sb.append("&logistics_orders=" + logisticCode + "");

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管查询物流单查询失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管物流单查询，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyLogisticResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyLogisticResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管物流单查询数据，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管物流单查询数据，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        if(!orderDTO.getCode().equals("0")){
            throw new ServiceException("TikTok全托管物流单查询失败:{}",orderDTO.getMessage());
        }
        return orderDTO;
    }

    /**
     * 取消发货
     * @param shopId 店铺id
     * @return
     */
    public void cancelLogistics(String shopId,String logisticCode) {
        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_shipment/202407/logistics_orders/cancel_ship";
        String toktikInfo = shopInfoDTO.getClientSecret();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body，平台用于计算签名
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("logistics_order",logisticCode);
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(bodyMap));
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管取消发货失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管取消发货，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyLogisticResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyLogisticResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管取消发货，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管取消发货，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        if(!orderDTO.getCode().equals("0")){
            throw new ServiceException("TikTok全托管取消发货失败:{}",orderDTO.getMessage());
        }
    }
    /**
     * 打印物流单
     * @param shopId 店铺id
     * @return url
     */
    public String printLogistics(String shopId,String logisticSubCode) {
        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_shipment/202405/beta/waybills";
        String toktikInfo = shopInfoDTO.getClientSecret();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);
        params.put("logistics_codes", logisticSubCode);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body，平台用于计算签名
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, "");
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");
        sb.append("&logistics_codes=" + logisticSubCode + "");

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管打印物流单失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管打印物流单，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyPrintDeliveryResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyPrintDeliveryResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管打印物流单，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管打印物流单，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        if(orderDTO.getCode()!=0){
            throw new ServiceException("TikTok全托管打印物流单失败:{}",orderDTO.getMessage());
        }
        return orderDTO.getData().getDocumentUrl();
    }

    /**
     * 确认发货
     * @param shopId 店铺id
     * @return
     */
    public void confirmDelivery(String shopId,String deliveryCode) {
        if(StringUtil.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        TikTokShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        String url = TikTokConstant.URL;
        StringBuffer sb = new StringBuffer();
        String path = "/gs_full_service_shipment/202405/beta/delivery_orders/confirm";
        String toktikInfo = shopInfoDTO.getClientSecret();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);


        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body，平台用于计算签名
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("delivery_order_code",deliveryCode);
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(bodyMap));
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(),  JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, TikTok全托管确认发货失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管确认发货，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        TikTokFullyBaseResp orderDTO = null;
        try {
            orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<TikTokFullyBaseResp>() {}.getType());
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok全托管确认发货数据，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管确认发货数据，返回值 responseMap={}",
                    url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        if(orderDTO.getCode()!=0){
            throw new ServiceException("TikTok全托管确认发货失败:{}",orderDTO.getMessage());
        }
    }
}
