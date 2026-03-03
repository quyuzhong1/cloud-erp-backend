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
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.fully.*;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyDeliveryOrderDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyOrderDTO;
import com.sdk.oms.tiktok.dto.tiktok.shop.TikTokShopAuthDTO;
import com.sdk.oms.tiktok.dto.tiktok.shop.ShopsBean;
import com.sdk.oms.tiktok.dto.tiktok.token.TokenDTO;
import com.sdk.oms.tiktok.dto.tiktok.token.PlatformTikTokTokenDTO;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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
    /**
     * 获取授权店铺列表
     * @param shopInfoDTO 店铺信息
     * @return TikTokShopAuthDTO 授权店铺信息
     */
    public static TikTokShopAuthDTO getAuthorizedShops(TikTokShopInfoDTO shopInfoDTO) {
        String url = TikTokConstant.URL;
        String path = "/authorization/" + TikTokConstant.VERSION + "/shops";
        String clientSecret = shopInfoDTO.getClientSecret();
        String clientId = shopInfoDTO.getClientId();
        
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("app_key", clientId);
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());
        headerMap.put("content-type", "multipart/form-data");

        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);
        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询授权店铺列表失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询授权店铺列表失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        
        //解析数据
        TikTokShopAuthDTO tikTokShopAuthDTO = null;
        try {
            tikTokShopAuthDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), TikTokShopAuthDTO.class);
        } catch (Exception e) {
            log.error("调用url={},入参params={}, TikTok查询授权店铺列表返回值 responseMap={}，转换成实体错误", url + path, params.toString(), JSONUtil.toJsonStr(apiResult), e);
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询授权店铺列表返回值 responseMap={}，转换成实体错误", 
                    apiResult.getData()));
        }
        
        return tikTokShopAuthDTO;
    }

    /**
     * 获取FBT仓库列表
     * @param shopInfoDTO 店铺信息（需要包含正确的shop_cipher）
     * @return 响应数据字符串
     */
    public static String getFbtWarehouseList(TikTokShopInfoDTO shopInfoDTO) {
        String url = TikTokConstant.URL;
        // Get FBT Warehouse List 接口路径
        String path = "/fbt/" + TikTokConstant.FBT_WAREHOUSE_VERSION + "/warehouses";
        String secret = shopInfoDTO.getClientSecret();
        
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        if (StringUtils.isNotBlank(shopInfoDTO.getShopCipher())) {
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
        }
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.FBT_WAREHOUSE_VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        // Get请求不需要body，空字符串即可
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, "");
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, secret);
        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + params.get("access_token") + "");
        sb.append("&app_key=" + params.get("app_key") + "");
        if (params.containsKey("shop_cipher")) {
            sb.append("&shop_cipher=" + params.get("shop_cipher") + "");
        }
        sb.append("&sign=" + params.get("sign") + "");
        sb.append("&timestamp=" + params.get("timestamp") + "");
        sb.append("&version=" + params.get("version") + "");

        //拉取数据 - Get请求
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            String errorMsg = "TikTok查询FBT仓库列表失败";
            String responseData = apiResult.getData();
            
            // 解析错误信息，提供更友好的提示
            if (Objects.equals(apiResult.getCode(), 401)) {
                try {
                    // 尝试解析错误响应
                    if (StringUtils.isNotBlank(responseData)) {
                        Map<String, Object> errorMap = JSONUtil.toBean(responseData, Map.class);
                        if (errorMap != null && errorMap.containsKey("code")) {
                            Integer errorCode = (Integer) errorMap.get("code");
                            String message = (String) errorMap.get("message");
                            
                            // 权限错误
                            if (errorCode != null && (errorCode == 105005 || message != null && message.contains("Access denied"))) {
                                errorMsg = StrUtil.format("权限错误(401): 应用或访问令牌缺少FBT相关的访问权限范围(scope)。\n" +
                                        "错误代码: {}\n" +
                                        "错误信息: {}\n" +
                                        "解决方案:\n" +
                                        "1. 在TikTok Shop Partner Center检查应用是否已申请FBT相关权限\n" +
                                        "2. 重新授权获取包含FBT权限的访问令牌\n" +
                                        "3. 联系TikTok Shop技术支持申请FBT相关权限\n" +
                                        "详情: https://m.tiktok.shop/s/AIu6dbFhs2XW", 
                                        errorCode, message);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析错误响应失败", e);
                }
            }
            
            log.error("调用url={},入参params={}, {}, 返回值 responseMap={}", 
                    sb.toString(), params.toString(), errorMsg, JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={}, {}, 返回值 responseMap={}",
                    sb.toString(), errorMsg, JSONUtil.toJsonStr(apiResult)));
        }

        //解析数据
        String responseData = apiResult.getData();
        log.info("Get FBT Warehouse List 响应数据: {}", responseData);
        return responseData;
    }

    /**
     * 获取入库订单列表 (Get Inbound Order)
     * 接口文档: https://partner.tiktokshop.com/docv2/page/get-inbound-order-202409
     * 
     * @param shopInfoDTO 店铺信息（需要包含正确的shop_cipher）
     * @param inboundOrderIds 入库订单ID列表（可选，如果提供则查询指定订单，否则查询时间范围内的所有订单）
     * @return 响应数据字符串
     */
    public static String getInboundOrder(TikTokShopInfoDTO shopInfoDTO, List<String> inboundOrderIds) {
        String url = TikTokConstant.URL;
        String path = "/fbt/" + TikTokConstant.FBT_INBOUND_ORDER_VERSION + "/inbound_orders";
        String secret = shopInfoDTO.getClientSecret();
        
        // 计算时间范围：当前时间前一个月
        long currentTime = System.currentTimeMillis() / 1000;
        long oneMonthAgo = LocalDateTime.now().minusMonths(1).toEpochSecond(ZoneOffset.ofHours(8));
        
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        if (StringUtils.isNotBlank(shopInfoDTO.getShopCipher())) {
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
        }
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.FBT_INBOUND_ORDER_VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        // Get Inbound Order接口可能是GET请求，参数放在URL中
        // 如果提供了入库订单ID列表，则查询指定订单；否则查询时间范围内的所有订单
        if (inboundOrderIds != null && CollectionUtil.isNotEmpty(inboundOrderIds)) {
            // 查询指定订单：兼容不同网关口径，同时传 ids 和 order_ids
            String inboundOrderIdsParam = inboundOrderIds.stream()
                    .map(TikTokFullService::normalizeInboundOrderId)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.joining(","));
            if (StrUtil.isNotBlank(inboundOrderIdsParam)) {
                params.put("ids", inboundOrderIdsParam);
                params.put("order_ids", inboundOrderIdsParam);
            }
        } else {
            // 查询时间范围内的所有订单
            params.put("create_time_start", oneMonthAgo);
            params.put("create_time_end", currentTime);
        }
        params.put("page_size", 50);
        
        //拉取数据 - GET请求，分页处理
        List<Map<String, Object>> allInboundOrders = new ArrayList<>();
        String pageToken = null;
        
        do {
            // 如果有分页token，添加到参数中
            if (pageToken != null) {
                params.put("page_token", pageToken);
            }
            
            // 更新时间戳（每次请求都需要新的时间戳）
            timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            
            // 重新计算签名
            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, "");
            String sign = EncryptionUtils.generateSHA256(input, secret);
            params.put("sign", sign);
            
            //组装url
            StringBuffer sb = new StringBuffer();
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + params.get("access_token") + "");
            sb.append("&app_key=" + params.get("app_key") + "");
            if (params.containsKey("shop_cipher")) {
                sb.append("&shop_cipher=" + params.get("shop_cipher") + "");
            }
            if (params.containsKey("create_time_start")) {
                sb.append("&create_time_start=" + params.get("create_time_start") + "");
            }
            if (params.containsKey("create_time_end")) {
                sb.append("&create_time_end=" + params.get("create_time_end") + "");
            }
            if (params.containsKey("ids")) {
                sb.append("&ids=" + params.get("ids"));
            }
            if (params.containsKey("order_ids")) {
                sb.append("&order_ids=" + params.get("order_ids"));
            }
            sb.append("&page_size=" + params.get("page_size") + "");
            if (params.containsKey("page_token")) {
                sb.append("&page_token=" + params.get("page_token") + "");
            }
            sb.append("&sign=" + params.get("sign") + "");
            sb.append("&timestamp=" + params.get("timestamp") + "");
            sb.append("&version=" + params.get("version") + "");
            
            log.info("请求入库订单 - URL: {}", sb.toString());
            ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                String errorData = apiResult.getData();
                log.error("调用url={}, TikTok获取入库订单失败，返回值 responseMap={}", sb.toString(), JSONUtil.toJsonStr(apiResult));
                
                // 尝试解析错误信息
                if (StringUtils.isNotBlank(errorData)) {
                    try {
                        Map<String, Object> errorMap = JSONUtil.toBean(errorData, Map.class);
                        if (errorMap != null && errorMap.containsKey("code")) {
                            Integer errorCode = (Integer) errorMap.get("code");
                            String message = (String) errorMap.get("message");
                            if (errorCode != null && errorCode == 40006) {
                                throw new RuntimeException(StrUtil.format("请求格式错误(40006): {}\n" +
                                        "可能原因：\n" +
                                        "1. 请求参数格式不正确\n" +
                                        "2. 缺少必需的参数\n" +
                                        "3. 参数名称或类型不正确\n" +
                                        "请检查请求URL: {}", 
                                        message, sb.toString()));
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                }
                
                throw new RuntimeException(StrUtil.format("调用url={}, TikTok获取入库订单失败，返回值 responseMap={}",
                        sb.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            String responseData = apiResult.getData() == null ? "{}" : apiResult.getData();
            Map<String, Object> responseMap = JSONUtil.toBean(responseData, Map.class);

            if (responseMap == null) {
                throw new RuntimeException(StrUtil.format("TikTok获取入库订单失败，返回值为空，url={}", sb.toString()));
            }
            if (!isSuccessResponseCode(responseMap.get("code"))) {
                throw new RuntimeException(StrUtil.format(
                        "TikTok获取入库订单失败，code={}, message={}, request_id={}, response={}",
                        responseMap.get("code"),
                        responseMap.get("message"),
                        responseMap.get("request_id"),
                        responseData));
            }
            Object dataObj = responseMap.get("data");
            if (!(dataObj instanceof Map)) {
                throw new RuntimeException(StrUtil.format(
                        "TikTok获取入库订单失败，data为空或格式错误，code={}, message={}, request_id={}, response={}",
                        responseMap.get("code"),
                        responseMap.get("message"),
                        responseMap.get("request_id"),
                        responseData));
            }
            Map<String, Object> dataMap = (Map<String, Object>) dataObj;
            if (dataMap.containsKey("inbound_orders")) {
                List<Map<String, Object>> inboundOrders = (List<Map<String, Object>>) dataMap.get("inbound_orders");
                if (CollectionUtil.isNotEmpty(inboundOrders)) {
                    allInboundOrders.addAll(inboundOrders);
                }
            }
            pageToken = (String) dataMap.get("next_page_token");
            // 移除page_token参数，为下次循环准备
            if (pageToken == null) {
                params.remove("page_token");
            }
        } while (StringUtils.isNotBlank(pageToken));

        log.info("Get Inbound Order 总共获取到 {} 条入库订单数据", allInboundOrders.size());
        return JSONUtil.toJsonStr(allInboundOrders);
    }

    private static String normalizeInboundOrderId(String inboundOrderId) {
        if (StrUtil.isBlank(inboundOrderId)) {
            return null;
        }
        String value = inboundOrderId.trim();
        if (StrUtil.startWithIgnoreCase(value, "IBR") && value.length() > 3) {
            value = value.substring(3);
        }
        return StrUtil.blankToDefault(value, null);
    }

    private static boolean isSuccessResponseCode(Object codeObj) {
        if (codeObj == null) {
            return true;
        }
        String code = String.valueOf(codeObj);
        if (StrUtil.isBlank(code)) {
            return true;
        }
        if ("0".equals(code) || "success".equalsIgnoreCase(code)) {
            return true;
        }
        try {
            return Integer.parseInt(code) == 0;
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * 搜索FBT库存 (Search FBT Inventory)
     * 接口文档: https://partner.tiktokshop.com/docv2/page/search-fbt-inventory-202408
     * 
     * @param shopInfoDTO 店铺信息（需要包含正确的shop_cipher）
     * @param goodsIds 商品ID列表（可选）
     * @param fbtWarehouseIds 仓库ID列表（可选）
     * @return 响应数据字符串
     */
    public static String searchFbtInventory(TikTokShopInfoDTO shopInfoDTO, List<String> goodsIds, List<String> fbtWarehouseIds) {
        String url = TikTokConstant.URL;
        String path = "/fbt/" + TikTokConstant.FBT_INVENTORY_VERSION + "/inventory/search";
        String secret = shopInfoDTO.getClientSecret();
        
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        if (StringUtils.isNotBlank(shopInfoDTO.getShopCipher())) {
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
        }
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.FBT_INVENTORY_VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body - 只包含过滤条件，不包含分页参数
        Map<String, Object> bodyMap = new HashMap<>();
//
//        // 如果提供了商品ID列表，添加到请求中
//        if (CollectionUtil.isNotEmpty(goodsIds)) {
//            bodyMap.put("goods_ids", goodsIds);
//        }
//
//        // 如果提供了仓库ID列表，添加到请求中
//        if (CollectionUtil.isNotEmpty(fbtWarehouseIds)) {
//            bodyMap.put("fbt_warehouse_ids", fbtWarehouseIds);
//        }
        
        String bodyJson = JSONUtil.toJsonStr(bodyMap);
        
        // page_size 和 page_token 放在 URL 参数中，不是 body 中
        params.put("page_size", 50);
        
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, bodyJson);
        String sign = EncryptionUtils.generateSHA256(input, secret);
        params.put("sign", sign);

        //拉取数据 - POST请求，分页处理
        List<Map<String, Object>> allInventory = new ArrayList<>();
        String pageToken = null;
        
        do {
            // 如果有分页token，添加到URL参数中
            if (pageToken != null) {
                params.put("page_token", pageToken);
            }
            
            // 更新时间戳（每次请求都需要新的时间戳）
            timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            
            // 重新计算签名（因为URL参数和时间戳改变了）
            input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, bodyJson);
            sign = EncryptionUtils.generateSHA256(input, secret);
            params.put("sign", sign);
            
            //组装url
            StringBuffer sb = new StringBuffer();
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + params.get("access_token") + "");
            sb.append("&app_key=" + params.get("app_key") + "");
            if (params.containsKey("shop_cipher")) {
                sb.append("&shop_cipher=" + params.get("shop_cipher") + "");
            }
            sb.append("&page_size=" + params.get("page_size") + "");
            if (params.containsKey("page_token")) {
                sb.append("&page_token=" + params.get("page_token") + "");
            }
            sb.append("&sign=" + params.get("sign") + "");
            sb.append("&timestamp=" + params.get("timestamp") + "");
            sb.append("&version=" + params.get("version") + "");
            
            ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), bodyJson, null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok搜索FBT库存失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok搜索FBT库存失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            String responseData = apiResult.getData();
            Map<String, Object> responseMap = JSONUtil.toBean(responseData, Map.class);
            
            if (responseMap != null && responseMap.containsKey("data")) {
                Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
                if (dataMap.containsKey("inventory_list")) {
                    List<Map<String, Object>> inventoryList = (List<Map<String, Object>>) dataMap.get("inventory_list");
                    if (CollectionUtil.isNotEmpty(inventoryList)) {
                        allInventory.addAll(inventoryList);
                    }
                }
                pageToken = (String) dataMap.get("next_page_token");
            } else {
                break;
            }
        } while (StringUtils.isNotBlank(pageToken));

        log.info("Search FBT Inventory 总共获取到 {} 条库存数据", allInventory.size());
        return JSONUtil.toJsonStr(allInventory);
    }

    /**
     * 搜索FBT库存流水记录 (Search FBT Inventory Record)
     * 接口文档: https://partner.tiktokshop.com/docv2/page/search-fbt-inventory-record-202410
     * 
     * @param shopInfoDTO 店铺信息（需要包含正确的shop_cipher）
     * @param goodsIds 商品ID列表（可选）
     * @param fbtWarehouseIds 仓库ID列表（可选）
     * @return 响应数据字符串
     */
    public static String searchFbtInventoryRecord(TikTokShopInfoDTO shopInfoDTO, List<String> goodsIds, List<String> fbtWarehouseIds) {
        String url = TikTokConstant.URL;
        String path = "/fbt/" + TikTokConstant.FBT_INVENTORY_RECORD_VERSION + "/inventory_records/search";
        String secret = shopInfoDTO.getClientSecret();
        
        // 计算时间范围：当前时间前一个月
        long currentTime = System.currentTimeMillis() / 1000;
        long oneMonthAgo = LocalDateTime.now().minusMonths(1).toEpochSecond(ZoneOffset.ofHours(8));
        
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        if (StringUtils.isNotBlank(shopInfoDTO.getShopCipher())) {
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
        }
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.FBT_INVENTORY_RECORD_VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body，包含时间范围参数和过滤条件，不包含分页参数
        Map<String, Object> bodyMap = new HashMap<>();
        // API要求使用 create_time_ge 和 create_time_le，不是 update_time_start 和 update_time_end
//        bodyMap.put("create_time_ge", 0);
//        bodyMap.put("create_time_le", currentTime);
        
//        // 如果提供了商品ID列表，添加到请求中
//        if (CollectionUtil.isNotEmpty(goodsIds)) {
//            bodyMap.put("goods_ids", goodsIds);
//        }
        
//        // 如果提供了仓库ID列表，添加到请求中
//        if (CollectionUtil.isNotEmpty(fbtWarehouseIds)) {
//            bodyMap.put("fbt_warehouse_ids", fbtWarehouseIds);
//        }
        
        String bodyJson = JSONUtil.toJsonStr(bodyMap);
        
        // page_size 和 page_token 放在 URL 参数中，不是 body 中
        params.put("page_size", 50);
        
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, bodyJson);
        String sign = EncryptionUtils.generateSHA256(input, secret);
        params.put("sign", sign);

        //拉取数据 - POST请求，分页处理
        List<Map<String, Object>> allRecords = new ArrayList<>();
        String pageToken = null;
        
        do {
            // 如果有分页token，添加到URL参数中
            if (pageToken != null) {
                params.put("page_token", pageToken);
            }
            
            // 更新时间戳（每次请求都需要新的时间戳）
            timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            
            // 重新计算签名（因为URL参数和时间戳改变了）
            input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, bodyJson);
            sign = EncryptionUtils.generateSHA256(input, secret);
            params.put("sign", sign);
            
            //组装url
            StringBuffer sb = new StringBuffer();
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + params.get("access_token") + "");
            sb.append("&app_key=" + params.get("app_key") + "");
            if (params.containsKey("shop_cipher")) {
                sb.append("&shop_cipher=" + params.get("shop_cipher") + "");
            }
            sb.append("&page_size=" + params.get("page_size") + "");
            if (params.containsKey("page_token")) {
                sb.append("&page_token=" + params.get("page_token") + "");
            }
            sb.append("&sign=" + params.get("sign") + "");
            sb.append("&timestamp=" + params.get("timestamp") + "");
            sb.append("&version=" + params.get("version") + "");
            
            ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), bodyJson, null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok搜索FBT库存流水记录失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok搜索FBT库存流水记录失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            String responseData = apiResult.getData();
            Map<String, Object> responseMap = JSONUtil.toBean(responseData, Map.class);
            
            if (responseMap != null && responseMap.containsKey("data")) {
                Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
                if (dataMap.containsKey("inventory_records")) {
                    List<Map<String, Object>> records = (List<Map<String, Object>>) dataMap.get("inventory_records");
                    if (CollectionUtil.isNotEmpty(records)) {
                        allRecords.addAll(records);
                    }
                }
                pageToken = (String) dataMap.get("next_page_token");
            } else {
                break;
            }
        } while (StringUtils.isNotBlank(pageToken));

        log.info("Search FBT Inventory Record 总共获取到 {} 条库存流水记录", allRecords.size());
        return JSONUtil.toJsonStr(allRecords);
    }

    /**
     * 搜索商品信息 (Search Goods Info)
     * 接口文档: https://partner.tiktokshop.com/docv2/page/search-goods-info-202409
     * 
     * @param shopInfoDTO 店铺信息（需要包含正确的shop_cipher）
     * @return 响应数据字符串
     */
    public static String searchGoodsInfo(TikTokShopInfoDTO shopInfoDTO) {
        String url = TikTokConstant.URL;
        String path = "/fbt/" + TikTokConstant.FBT_GOODS_INFO_VERSION + "/goods/search";
        String secret = shopInfoDTO.getClientSecret();
        
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", shopInfoDTO.getAccessToken());
        params.put("app_key", shopInfoDTO.getClientId());
        if (StringUtils.isNotBlank(shopInfoDTO.getShopCipher())) {
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
        }
        String timestamp = System.currentTimeMillis() / 1000 + "";
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.FBT_GOODS_INFO_VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

        //请求body - 只包含过滤条件（goods_ids、product_ids、reference_codes、sku_ids等）
        Map<String, Object> bodyMap = new HashMap<>();
        String bodyJson = JSONUtil.toJsonStr(bodyMap);
        
        // page_size 和 page_token 放在 URL 参数中，不是 body 中
        params.put("page_size", 50);
        
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, bodyJson);
        String sign = EncryptionUtils.generateSHA256(input, secret);
        params.put("sign", sign);

        //拉取数据 - POST请求，分页处理
        List<Map<String, Object>> allGoods = new ArrayList<>();
        String pageToken = null;
        
        do {
            // 如果有分页token，添加到URL参数中
            if (pageToken != null) {
                params.put("page_token", pageToken);
            }
            
            // 更新时间戳（每次请求都需要新的时间戳）
            timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            
            // 重新计算签名（因为URL参数和时间戳改变了）
            input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, bodyJson);
            sign = EncryptionUtils.generateSHA256(input, secret);
            params.put("sign", sign);
            
            //组装url
            StringBuffer sb = new StringBuffer();
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + params.get("access_token") + "");
            sb.append("&app_key=" + params.get("app_key") + "");
            if (params.containsKey("shop_cipher")) {
                sb.append("&shop_cipher=" + params.get("shop_cipher") + "");
            }
            sb.append("&page_size=" + params.get("page_size") + "");
            if (params.containsKey("page_token")) {
                sb.append("&page_token=" + params.get("page_token") + "");
            }
            sb.append("&sign=" + params.get("sign") + "");
            sb.append("&timestamp=" + params.get("timestamp") + "");
            sb.append("&version=" + params.get("version") + "");
            
            log.info("请求商品信息 - URL: {}, Body: {}", sb.toString(), bodyJson);
            ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), bodyJson, null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={}, body={}, TikTok搜索商品信息失败，返回值 responseMap={}", sb.toString(), bodyJson, JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={}, body={}, TikTok搜索商品信息失败，返回值 responseMap={}",
                        sb.toString(), bodyJson, JSONUtil.toJsonStr(apiResult)));
            }

            String responseData = apiResult.getData();
            Map<String, Object> responseMap = JSONUtil.toBean(responseData, Map.class);
            
            if (responseMap != null && responseMap.containsKey("data")) {
                Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
                // API响应字段是 goods，不是 goods_list
                if (dataMap.containsKey("goods")) {
                    List<Map<String, Object>> goodsList = (List<Map<String, Object>>) dataMap.get("goods");
                    if (CollectionUtil.isNotEmpty(goodsList)) {
                        allGoods.addAll(goodsList);
                    }
                }
                pageToken = (String) dataMap.get("next_page_token");
            } else {
                break;
            }
        } while (StringUtils.isNotBlank(pageToken));

        log.info("Search Goods Info 总共获取到 {} 条商品数据", allGoods.size());
        return JSONUtil.toJsonStr(allGoods);
    }

    /**
     * 刷新访问令牌
     * 说明：
     * 1. 刷新令牌是安全的操作，不会影响线上店铺的正常运营
     * 2. 刷新令牌只是获取新的 access_token 和 refresh_token，不会改变店铺授权关系
     * 3. 旧的 access_token 在过期前仍然有效，新旧的 token 可以同时使用一段时间
     * 4. 刷新令牌不会影响店铺的订单、库存、发货等任何业务操作
     * 
     * @param shopInfoDTO 店铺信息（需要包含 refresh_token）
     * @param baseUrl 授权服务器地址，通常是 "https://auth.tiktok-shops.com"
     * @return TokenDTO 包含新的 access_token 和 refresh_token
     */
    public static TokenDTO refreshAccessToken(TikTokShopInfoDTO shopInfoDTO, String baseUrl, String refreshToken) {
        // 组装刷新token的URL
        String path = "/api/v2/token/refresh?app_key=%s&app_secret=%s&refresh_token=%s&grant_type=refresh_token";
        String url = String.format(baseUrl + path, 
                shopInfoDTO.getClientId(), 
                shopInfoDTO.getClientSecret(), 
                refreshToken);
        
        // 请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("accept", "application/json");
        
        // 发起GET请求（虽然路径中有参数，但TikTok API要求用GET方法）
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url, JSONUtil.toJsonStr(new HashMap<>()), null, headerMap, RequestMethod.GET);
        
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={}, TikTok刷新token失败，返回值 responseMap={}", url, JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={}, TikTok刷新token失败，返回值 responseMap={}",
                    url, JSONUtil.toJsonStr(apiResult)));
        }
        
        // 解析数据
        PlatformTikTokTokenDTO tikTokTokenDTO = null;
        try {
            tikTokTokenDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), PlatformTikTokTokenDTO.class);
        } catch (Exception e) {
            log.error("TikTok刷新token返回值解析失败，返回值 responseMap={}", JSONUtil.toJsonStr(apiResult.getData()), e);
            throw new RuntimeException(StrUtil.format("TikTok刷新token返回值解析失败，返回值 responseMap={}",
                    JSONUtil.toJsonStr(apiResult.getData())));
        }
        
        if (tikTokTokenDTO == null || tikTokTokenDTO.getData() == null || 
            StringUtils.isBlank(tikTokTokenDTO.getData().getAccessToken())) {
            throw new ServiceException("TikTok刷新token失败：返回的token为空");
        }
        
        TokenDTO tokenDTO = tikTokTokenDTO.getData();
        log.info("刷新token成功 - 新access_token: {}, 新refresh_token: {}, 过期时间: {}秒", 
                tokenDTO.getAccessToken().substring(0, Math.min(20, tokenDTO.getAccessToken().length())) + "...",
                tokenDTO.getRefreshToken() != null ? tokenDTO.getRefreshToken().substring(0, Math.min(20, tokenDTO.getRefreshToken().length())) + "..." : "null",
                tokenDTO.getAccessTokenExpireIn());
        
        return tokenDTO;
    }

    /**
     * 刷新令牌示例方法
     * 使用说明：
     * 1. 需要传入当前有效的 refresh_token
     * 2. refresh_token 通常有效期较长（如90天），可以在 access_token 过期前随时刷新
     * 3. 刷新后需要更新数据库中保存的 access_token 和 refresh_token
     */
    public static void refreshTokenExample() {
        TikTokShopInfoDTO shopInfoDTO = new TikTokShopInfoDTO();
        shopInfoDTO.setClientId("6buinkjt3hmld");
        shopInfoDTO.setClientSecret("8ff628de24faf70c24855de4d967fb6a17a47e3f");
        
        // 重要：这里需要传入当前有效的 refresh_token
        // refresh_token 通常在首次授权时获取，需要从数据库中读取
        String refreshToken = "YOUR_REFRESH_TOKEN_HERE"; // 请替换为实际的refresh_token
        String baseUrl = "https://auth.tiktok-shops.com";
        
        try {
            log.info("开始刷新访问令牌...");
            TokenDTO newToken = refreshAccessToken(shopInfoDTO, baseUrl, refreshToken);
            
            log.info("刷新成功！");
            log.info("新的 access_token: {}", newToken.getAccessToken());
            log.info("新的 refresh_token: {}", newToken.getRefreshToken());
            log.info("access_token 过期时间（秒）: {}", newToken.getAccessTokenExpireIn());
            
            // 重要：刷新成功后，需要更新数据库中的 token 信息
            // shopAuthService.updateToken(shopId, newToken.getAccessToken(), newToken.getRefreshToken(), ...);
            
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        TikTokShopInfoDTO shopInfoDTO = new TikTokShopInfoDTO();
        shopInfoDTO.setClientId("6buinkjt3hmld");
        shopInfoDTO.setClientSecret("8ff628de24faf70c24855de4d967fb6a17a47e3f");
        shopInfoDTO.setAccessToken("ROW_K28jIgAAAACj-JAAAriAWjVtF2MrUIFdPkU8v2PwmslxLKqRdTTD6LC69bn3X7wtL1fvhLtlULbb9xoOJGkUBexq78KD43AkOaiNHMBhmGQF1fEt3fHZNLvsm5Zv5lXdhf1PSh6Dw5kVnYg4vbEocTe-OCFF3_ljS-kXgyWbrORu18_NygL94u8aSWDmQMTmseuly-WF3yk");
        
        // 第一步：获取授权店铺列表，获取正确的shop_cipher
        log.info("开始获取授权店铺列表...");
        TikTokShopAuthDTO shopAuthDTO = getAuthorizedShops(shopInfoDTO);
        
        if (shopAuthDTO == null || shopAuthDTO.getData() == null || 
            CollectionUtil.isEmpty(shopAuthDTO.getData().getShops())) {
            log.error("未获取到授权店铺信息");
            throw new RuntimeException("未获取到授权店铺信息");
        }
        
        // 获取第一个店铺的shop_cipher
        ShopsBean firstShop = shopAuthDTO.getData().getShops().get(0);
        String shopCipher = firstShop.getCipher();
        log.info("获取到店铺信息 - shop_id: {}, shop_cipher: {}, shop_name: {}", 
                firstShop.getId(), shopCipher, firstShop.getName());
        
        // 设置正确的shop_cipher
        shopInfoDTO.setShopCipher(shopCipher);
        
        try {
            // 第一步：获取FBT仓库列表
            log.info("\n========== 第一步：获取FBT仓库列表 ==========");
            String warehouseListResponse = getFbtWarehouseList(shopInfoDTO);
            log.info("FBT仓库列表响应数据: {}", warehouseListResponse);
            
            // 解析仓库列表，提取仓库ID
            List<String> fbtWarehouseIds = new ArrayList<>();
            try {
                Map<String, Object> warehouseResponseMap = JSONUtil.toBean(warehouseListResponse, Map.class);
                if (warehouseResponseMap != null && warehouseResponseMap.containsKey("data")) {
                    Map<String, Object> dataMap = (Map<String, Object>) warehouseResponseMap.get("data");
                    if (dataMap.containsKey("warehouses")) {
                        List<Map<String, Object>> warehouses = (List<Map<String, Object>>) dataMap.get("warehouses");
                        if (CollectionUtil.isNotEmpty(warehouses)) {
                            for (Map<String, Object> warehouse : warehouses) {
                                // 仓库ID字段是 fbt_warehouse_id
                                String warehouseId = (String) warehouse.get("fbt_warehouse_id");
                                if (StringUtils.isBlank(warehouseId)) {
                                    // 如果没有 fbt_warehouse_id，尝试其他字段
                                    warehouseId = (String) warehouse.get("entity_id");
                                    if (StringUtils.isBlank(warehouseId)) {
                                        warehouseId = (String) warehouse.get("id");
                                    }
                                }
                                if (StringUtils.isNotBlank(warehouseId)) {
                                    fbtWarehouseIds.add(warehouseId);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析仓库列表失败，将使用空仓库ID列表: {}", e.getMessage(), e);
            }
            log.info("提取到 {} 个仓库ID: {}", fbtWarehouseIds.size(), fbtWarehouseIds);
            
            // 第二步：获取商品信息
            log.info("\n========== 第二步：获取商品信息 ==========");
            String goodsInfoResponse = searchGoodsInfo(shopInfoDTO);
            log.info("商品信息响应数据: {}", goodsInfoResponse);
            
            // 解析商品列表，提取商品ID和SKU ID
            List<String> goodsIds = new ArrayList<>();
            List<String> skuIds = new ArrayList<>();
            try {
                // searchGoodsInfo 返回的是商品数组的JSON字符串
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> goodsList = (List<Map<String, Object>>) (List<?>) JSONUtil.toList(goodsInfoResponse, Map.class);
                if (CollectionUtil.isNotEmpty(goodsList)) {
                    for (Map<String, Object> goods : goodsList) {
                        // 商品ID字段是 id
                        String goodsId = (String) goods.get("id");
                        if (StringUtils.isNotBlank(goodsId)) {
                            goodsIds.add(goodsId);
                        }
                        
                        // 提取SKU ID
                        if (goods.containsKey("skus")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> skus = (List<Map<String, Object>>) goods.get("skus");
                            if (CollectionUtil.isNotEmpty(skus)) {
                                for (Map<String, Object> sku : skus) {
                                    String skuId = (String) sku.get("id");
                                    if (StringUtils.isNotBlank(skuId)) {
                                        skuIds.add(skuId);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析商品列表失败，将使用空商品ID列表: {}", e.getMessage(), e);
            }
            log.info("提取到 {} 个商品ID: {}", goodsIds.size(), goodsIds);
            log.info("提取到 {} 个SKU ID: {}", skuIds.size(), skuIds);
            
            // 第三步：使用 goods_ids 和 fbt_warehouse_ids 查询库存流水
            log.info("\n========== 第三步：查询库存流水记录 ==========");
            String inventoryRecordResponse = searchFbtInventoryRecord(shopInfoDTO, goodsIds, fbtWarehouseIds);
            log.info("库存流水记录响应数据: {}", inventoryRecordResponse);
            
            // 解析库存流水，提取 INBOUND_ORDER 类型的订单ID
            List<String> inboundOrderIds = new ArrayList<>();
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> recordsList = (List<Map<String, Object>>) (List<?>) JSONUtil.toList(inventoryRecordResponse, Map.class);
                if (CollectionUtil.isNotEmpty(recordsList)) {
                    for (Map<String, Object> record : recordsList) {
                        // 检查订单类型是否为 INBOUND_ORDER
                        Map<String, Object> order = (Map<String, Object>) record.get("order");
                        if (order != null) {
                            String orderType = (String) order.get("type");
                            if ("INBOUND_ORDER".equals(orderType)) {
                                String orderId = (String) order.get("id");
                                if (StringUtils.isNotBlank(orderId) && !inboundOrderIds.contains(orderId)) {
                                    inboundOrderIds.add(orderId);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析库存流水记录失败: {}", e.getMessage());
            }
            log.info("提取到 {} 个入库订单ID: {}", inboundOrderIds.size(), inboundOrderIds);
            
            // 第四步：使用入库订单ID查询入库订单详情
            log.info("\n========== 第四步：查询入库订单详情 ==========");
            String inboundOrderResponse = null;
            if (CollectionUtil.isNotEmpty(inboundOrderIds)) {
                inboundOrderResponse = getInboundOrder(shopInfoDTO, inboundOrderIds);
                log.info("入库订单详情响应数据: {}", inboundOrderResponse);
            } else {
                log.warn("未找到入库订单ID，跳过查询入库订单详情");
            }
            
            // 第五步：使用 goods_ids 和 fbt_warehouse_ids 查询库存
            log.info("\n========== 第五步：查询库存 ==========");
            String inventoryResponse = searchFbtInventory(shopInfoDTO, goodsIds, fbtWarehouseIds);
            log.info("库存响应数据: {}", inventoryResponse);
            
            log.info("\n========== 所有数据获取完成 ==========");
            log.info("仓库数量: {}", fbtWarehouseIds.size());
            log.info("商品数量: {}", goodsIds.size());
            log.info("入库订单数量: {}", inboundOrderIds.size());
            
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("权限错误")) {
                System.err.println("\n========== 权限错误说明 ==========");
                System.err.println(e.getMessage());
                System.err.println("\n重要提示：");
                System.err.println("刷新token不会增加新权限！刷新token只会续期，不会改变权限范围(scope)。");
                System.err.println("\n解决方案：");
                System.err.println("1. 检查应用权限：登录 TikTok Shop Partner Center");
                System.err.println("   https://partner.tiktokshop.com");
                System.err.println("   查看应用是否已申请 FBT (Fulfillment by TikTok) 相关权限");
                System.err.println("\n2. 如果应用有FBT权限，但token没有：");
                System.err.println("   需要重新授权，而不是刷新token");
                System.err.println("   重新授权步骤：");
                System.err.println("   a. 在系统中找到该店铺的重新授权入口");
                System.err.println("   b. 或者在 TikTok Shop Partner Center 重新授权应用");
                System.err.println("   c. 授权时确保勾选所有需要的权限范围（包括FBT相关权限）");
                System.err.println("\n3. 如果应用本身没有FBT权限：");
                System.err.println("   联系 TikTok Shop 技术支持申请 FBT API 访问权限");
                System.err.println("   详情: https://m.tiktok.shop/s/AIu6dbFhs2XW");
                System.err.println("\n4. 重新授权后，新的token会包含所有已申请的权限");
                System.err.println("   刷新token = 续期，不改变权限");
                System.err.println("   重新授权 = 获取新权限");
                System.err.println("\n⚠️  重要提示：重新授权对线上token的影响");
                System.err.println("   - 重新授权会生成新token并更新数据库和Redis缓存");
                System.err.println("   - 旧token可能会立即失效（取决于TikTok实现）");
                System.err.println("   - 建议在业务低峰期进行重新授权");
                System.err.println("   - 或者先测试新token可用性，再切换");
                System.err.println("   - 重新授权后建议监控一段时间，确保业务正常");
                System.err.println("============================\n");
            }
            throw e;
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
