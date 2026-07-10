package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.DataIdempotent;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.api.TokensApi;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.*;
import com.erp.sdk.oms.amz.spapi.model.tokens.CreateRestrictedDataTokenRequest;
import com.erp.sdk.oms.amz.spapi.model.tokens.CreateRestrictedDataTokenResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiRateLimitUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 亚马逊订单处理器
 *
 * @Author Cloud
 * @Date 2023/8/31 15:48
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.AMAZON)
@BusinessType(BusinessTypeEnum.ORDER)
public class AmazonOrderHandler extends AbstractOrderHandler<PlatformAmazonOrderDTO, PlatformOrderDTO> {

    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private AmazonSpApiRateLimitUtils amazonSpApiRateLimitUtils;

    @Override
    public List<PlatformAmazonOrderDTO> download(JobTaskDTO data) {
        // 获取店铺信息
        String shopId = data.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
//        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_LIST;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX, data.getGroupId());
//        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj){
            String msg = StrUtil.format("【订单拉取】 taskId={}, groupId={},存在429等待恢复:放弃当前请求任务", data.getId(), data.getGroupId());
            throw new ServiceException(msg);
        }
        // 开始时间缓存key
        LocalDateTime orderLastUpdateTime = null;
        String orderStartTimeKey = StrUtil.format(RedisCacheConstants.AMAZON_ORDER_TASK_TIME_PREFIX, data.getId());
        Object orderLastUpdateTimeObj = redisUtil.get(orderStartTimeKey);
        if (null != orderLastUpdateTimeObj){
            if (orderLastUpdateTimeObj instanceof LocalDateTime){
                orderLastUpdateTime =  (LocalDateTime) orderLastUpdateTimeObj;
            } else {
                orderLastUpdateTime = LocalDateTimeUtil.parse(orderLastUpdateTimeObj.toString(), "yyyy-MM-dd HH:mm:ss");
            }
        }

        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.ORDER_LIST.getRateLimit();
        // 根据亚马逊的响应时间记录下次执行开始时间
        LocalDateTime nextStartTime;

        // 亚马逊订单下载
//        OrdersV0Api api = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
        OrdersV0Api api = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
        // 正式环境请求
        // 东八区转UTC时间
        // 取开始时间最小值
        LocalDateTime lastUpdatedAfterLocal;
        if (null == orderLastUpdateTime){
            lastUpdatedAfterLocal = data.getLastTime();
        } else {
            lastUpdatedAfterLocal = data.getLastTime().isAfter(orderLastUpdateTime) ? orderLastUpdateTime : data.getLastTime();
        }
        String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(lastUpdatedAfterLocal).toString();
        String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(data.getNextTime()).toString();
        try {
//            List<String> marketplaceIds = Collections.singletonList(marketPlaceEnum.getMarketplaceId());
            // 请求全站点
            List<String> marketplaceIds = new ArrayList<>(shopInfoDTO.getMarketplaceShopIdMap().keySet());
            // 发起请求
            ApiResponse<GetOrdersResponse> ordersWithHttpInfo = api.getOrdersWithHttpInfo(marketplaceIds,
                    null, null, lastUpdatedAfter, lastUpdatedBefore, null, null, null, null, null, 100,
                    null, null, null, null, null, null, null, null, null, null, null);
            List<String> limitArray = ordersWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetOrdersResponse orders = ordersWithHttpInfo.getData();
            // 亚马逊接口响应时间UTC转换8区
            LocalDateTime parse = LocalDateTime.parse(orders.getPayload().getLastUpdatedBefore(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            nextStartTime = DateUtil.utcSamePlus8(parse);

            List<Order> orderList = new LinkedList<>(orders.getPayload().getOrders());
            String currentNextToken = orders.getPayload().getNextToken();
            int currentSize = orders.getPayload().getOrders().size();
            while (StringUtils.isNotBlank(currentNextToken) && currentSize == 100) {
                // 上一次请求的响应频率设置
//                if (StringUtils.isNotBlank(rateLimitStr)){
//                    RateLimitConfigurationOnRequests rateLimitConfigurationRequests = (RateLimitConfigurationOnRequests) rateLimitConfig;
//                    rateLimitConfigurationRequests.setRateLimitPermit(Double.parseDouble(rateLimitStr));
//                    api.getApiClient().setRateLimiter(rateLimitConfigurationRequests);
//                }
                GetOrdersResponse currentResp = api.getOrders(marketplaceIds, null, null, null, null, null, null, null, null, null, 100, null, null, currentNextToken, null, null, null, null, null, null, null, null);
                orderList.addAll(currentResp.getPayload().getOrders());
                // 亚马逊接口响应时间UTC转换8区
//                LocalDateTime currentParse = LocalDateTime.parse(currentResp.getPayload().getLastUpdatedBefore(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//                nextStartTime = DateUtil.utcSamePlus8(currentParse);
                currentNextToken = currentResp.getPayload().getNextToken();
                currentSize = currentResp.getPayload().getOrders().size();
                List<String> currentLimitArray = ordersWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                rateLimitStr = currentLimitArray.get(0);
            }
//            if (StringUtils.isNotBlank(rateLimitStr)){
//                // 设置动态速率，失效时间=1/limit
//                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
//                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
//            }
            // （临时）缓存设置根据亚马逊的响应时间记录下次执行开始时间
            // 前置10分钟（防止订单状态延时更新)
            nextStartTime = nextStartTime.minusMinutes(10);
            redisUtil.set(orderStartTimeKey, nextStartTime);

            // 返回下载源数据
            return orderList.stream()
                    .map(e-> new PlatformAmazonOrderDTO(e, shopInfoDTO))
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            if (429 == e.getCode()){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            throw new RuntimeException("请求亚马逊SP-APi订单api异常失败,body=" + JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new RuntimeException("请求亚马逊SP-APi订单失败,body=" + JSONUtil.toJsonStr(e));
        }
    }


    @Override
    public List<PlatformOrderDTO> convert(List<PlatformAmazonOrderDTO> sourceDataList) {
        //亚马逊订单转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(e-> PlatformAmazonOrderDTO.convertDTO(e, this.getIsSendMq()))
                .collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }

    /**
     * 是否发送MQ
     * true=发送
     * false=不发送（有其他详情需要额外拉取）
     */
    @Override
    public Boolean getIsSendMq() {
        return Boolean.FALSE;
    }

    @Override
    @DataIdempotent(keyIdName = "dto.redissonKey", waitTime = 20)
    public PlatformAmazonOrderDTO downloadDetail(PlatformAmazonOrderDTO dto, JSONObject extendObj) {
        // 缓存获取
        String key = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS.getBusinessTypeName(), dto.getUniqueId());
        Object resultObj = redisUtil.get(key);
        if (null != resultObj) {
            return JSONUtil.toBean(resultObj.toString(), PlatformAmazonOrderDTO.class);
        }
        // 检查来源
        if (null == dto.getOrder()){
            String msg = StrUtil.format("订单来源为空:{}", JSONUtil.toJsonStr(dto));
            throw new ServiceException(msg);
        }
        if (StringUtils.isBlank(dto.getOrder().getAmazonOrderId())){
            String msg = StrUtil.format("订单来源ID为空:{}", JSONUtil.toJsonStr(dto));
            throw new ServiceException(msg);
        }

//        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS;
        // 默认请求速率配置
        String limitKey = extendObj.getString(AmazonRequestTypeRateLimiterEnum.limitKey);
//        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj){
            String msg = StrUtil.format("【订单明细拉取】 UniqueId={}, platformShopCode={},存在429等待恢复:放弃当前请求任务", dto.getUniqueId(), dto.getPlatformShopCode());
            throw new ServiceException(msg);
        }
        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS.getRateLimit();

        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = getAmazonShopInfoDTO(dto);
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 查询订单详情
//        OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
        OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
        OrderItemList allOrderItems = null;
        try {
            String orderId = dto.getOrder().getAmazonOrderId();
            ApiResponse<GetOrderItemsResponse> itemResponse = ordersVoApi.getOrderItemsWithHttpInfo(orderId, null);
            List<String> limitArray = itemResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetOrderItemsResponse orderItems = itemResponse.getData();

            String currentNextToken = orderItems.getPayload().getNextToken();
            OrderItemList resultOrderItemsList = orderItems.getPayload().getOrderItems();
            while (StringUtils.isNotBlank(currentNextToken)) {
                ApiResponse<GetOrderItemsResponse> currentOrderItemsResp = ordersVoApi.getOrderItemsWithHttpInfo(orderId, currentNextToken);
                GetOrderItemsResponse currentOrderItems = currentOrderItemsResp.getData();
                List<String> currentLimitArray = itemResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                rateLimitStr = currentLimitArray.get(0);
                resultOrderItemsList.addAll(currentOrderItems.getPayload().getOrderItems());
                currentNextToken = currentOrderItems.getPayload().getNextToken();
            }
            allOrderItems = resultOrderItemsList;

//            if (StringUtils.isNotBlank(rateLimitStr)){
//                // 设置动态速率，失效时间=1/limit
//                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
//                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
//            }
        } catch (ApiException e) {
            if (429 == e.getCode()){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            throw new ServiceException("查询亚马逊订单详情失败：API异常："+JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单详情失败："+JSONUtil.toJsonStr(e));
        }
        if (CollectionUtils.isEmpty(allOrderItems)){
            return dto;
        }
        dto.setDetails(allOrderItems);
        // 缓存倒redis
        redisUtil.set(key, JSONUtil.toJsonStr(dto), 300);
        log.info("查询亚马逊订单详情成功, UniqueId={}", dto.getUniqueId());
        return dto;
    }

    /**
     * 请求RDT授权
     */
    public String queryAndGetRDT(String orderId, AmazonShopInfoDTO shopInfoDTO) {
        // 生成RDT权限获取地址信息
        // amazon-rdt-token:平台账号ID:订单ID
        String tokenKey = StrUtil.format(RedisCacheConstants.AMAZON_RDT_TOKEN, shopInfoDTO.getPlatformShopCode(), orderId);
        Object obj = redisUtil.get(tokenKey);
        // 当前RDT
        String rdtToken = "";
        if (null != obj){
            rdtToken = (String) obj;
        } else {
            AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            TokensApi api = TokensApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false);
            CreateRestrictedDataTokenRequest body = CreateRestrictedDataTokenRequest.builderByOrderId(orderId);
            try {
                CreateRestrictedDataTokenResponse response = api.createRestrictedDataToken(body);
                rdtToken = response.getRestrictedDataToken();
                // 缓存到redis
                redisUtil.set(tokenKey, rdtToken, response.getExpiresIn());
            } catch (Exception e) {
                throw new ServiceException("获取亚马逊订单RDT token失败："+ JSONUtil.toJsonStr(e));
            }
        }
        if (StringUtils.isBlank(rdtToken)){
            throw new ServiceException("亚马逊RDT token异常：orderId：" + orderId);
        }
        return rdtToken;
    }


    @DataIdempotent(keyIdName = "dto.redissonKey", waitTime = 20)
    public PlatformAmazonOrderDTO downloadAddressAndBuyInfo(PlatformAmazonOrderDTO dto, JSONObject extendObj) {
        if ( null != dto.getOrder().getShippingAddress() &&
                StringUtils.isNotBlank(dto.getOrder().getShippingAddress().getName())){
            // 已有信息不请求
            log.info("亚马逊详情和地址已有不请求, UniqueId={}", dto.getUniqueId());
            return dto;
        }
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = getAmazonShopInfoDTO(dto);
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 请求速率配置
//        String limitKey = extendObj.getString(AmazonRequestTypeRateLimiterEnum.limitKey);
//        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS;
//        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);
//        OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
        OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
        // 查询订单详情

        // 生成RDT权限获取地址信息
        // amazon-rdt-token:店铺ID:订单ID
        String rdtToken = queryAndGetRDT(dto.getOrder().getAmazonOrderId(), shopInfoDTO);

        // 修改x-amz-access-token的token
        ordersVoApi.getApiClient().addDefaultHeader(ApiClient.SIGNED_ACCESS_TOKEN_HEADER_NAME, rdtToken);

        // 设置地址
        Address shippingAddress = downloadAddress(shopInfoDTO.getPlatformShopCode(), dto.getOrder().getAmazonOrderId(), ordersVoApi);
        Order order = dto.getOrder();
        order.setShippingAddress(shippingAddress);

        // 设置买家信息
        OrderBuyerInfo buyerInfo = downloadBuyer(dto.getPlatformShopCode(), dto.getOrder().getAmazonOrderId(), ordersVoApi);
        BeanUtils.copyProperties(buyerInfo, order.getBuyerInfo());
        return dto;
    }

    /**
     * 下载亚马逊地址信息
     * @param platformShopCode 平台账号
     * @param orderId   订单ID
     * @param ordersVoApi 订单客户端
     * @return  亚马逊地址对象
     */
    public Address downloadAddress(String platformShopCode, String orderId, OrdersV0Api ordersVoApi) {
        String uniqueId = StrUtil.format("{}_{}", platformShopCode, orderId);
        String key = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getBusinessTypeName(), uniqueId);
        Object resultObj = redisUtil.get(key);
        if (null != resultObj) {
            return JSONUtil.toBean(resultObj.toString(), Address.class);
        }
        // 获取动态速率
        // 平台请求中:平台类型:sellerId:业务类型
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS;
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), platformShopCode, requestTypeRateLimiterEnum.getBusinessTypeName());
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj){
            String msg = StrUtil.format("【订单地址拉取】 UniqueId={},存在429等待恢复:放弃当前请求任务", uniqueId);
            throw new ServiceException(msg);
        }
        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getRateLimit();
        try {
            ApiResponse<GetOrderAddressResponse> orderAddressResp = ordersVoApi.getOrderAddressWithHttpInfo(orderId);
            List<String> limitArray = orderAddressResp.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetOrderAddressResponse response = orderAddressResp.getData();
            // 结果缓存倒redis(消费完移除)
            redisUtil.set(key, JSONUtil.toJsonStr(response.getPayload().getShippingAddress()), 300);
            return response.getPayload().getShippingAddress();
        } catch (ApiException e) {
            if (429 == e.getCode()){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            throw new ServiceException("查询亚马逊订单地址失败：API异常："+JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单地址失败："+JSONUtil.toJsonStr(e));
        }
    }

    /**
     * 下载买家信息
     * @param platformShopCode 平台账号
     * @param orderId   订单ID
     * @param ordersVoApi 订单客户端
     * @return  亚马逊买家对象
     */
    public OrderBuyerInfo downloadBuyer(String platformShopCode, String orderId, OrdersV0Api ordersVoApi) {
        String uniqueId = StrUtil.format("{}_{}", platformShopCode, orderId);
        String key = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.BUYER_INFO.getBusinessTypeName(), uniqueId);
        Object resultObj = redisUtil.get(key);
        if (null != resultObj) {
            return JSONUtil.toBean(resultObj.toString(), OrderBuyerInfo.class);
        }
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.BUYER_INFO;
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), platformShopCode, requestTypeRateLimiterEnum.getBusinessTypeName());
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj){
            String msg = StrUtil.format("【订单买家信息拉取】 platformShopCode={},存在429等待恢复:放弃当前请求任务", platformShopCode);
            throw new ServiceException(msg);
        }
        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.BUYER_INFO.getRateLimit();
        try {
            ApiResponse<GetOrderBuyerInfoResponse> orderBuyerInfoResp = ordersVoApi.getOrderBuyerInfoWithHttpInfo(orderId);
            List<String> limitArray = orderBuyerInfoResp.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetOrderBuyerInfoResponse response = orderBuyerInfoResp.getData();
            if (StringUtils.isNotBlank(rateLimitStr)){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            // 结果缓存倒redis(消费完移除)
            redisUtil.set(key, JSONUtil.toJsonStr(response.getPayload()), 300);
            return response.getPayload();
        } catch (ApiException e) {
            if (429 == e.getCode()){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            throw new ServiceException("查询亚马逊订单地址失败：API异常："+JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单买家信息失败："+JSONUtil.toJsonStr(e));
        }
    }

    /**
     * 获取店铺授权信息
     */
    private AmazonShopInfoDTO getAmazonShopInfoDTO(PlatformAmazonOrderDTO dto) {
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(dto.getShopId());
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + dto.getShopId());
        }
        return shopInfoDTO;
    }


    public List<PlatformAmazonOrderDTO> downloadByOrderIds(Map<String, String> dtoOrderIdShopIdMap, String shopId, String groupId, List<CfgTimezoneEntity> timeZoneList) {
        List<String> orderIds = new ArrayList<>(dtoOrderIdShopIdMap.keySet());
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_LIST;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX, groupId);
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj){
            String msg = StrUtil.format("【亚马逊根据订单IDS拉取订单】 groupId={},存在429等待恢复:放弃当前请求任务", groupId);
            throw new ServiceException(msg);
        }

        // 亚马逊订单下载
        OrdersV0Api api = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
        // 正式环境请求
        try {
//            List<String> marketplaceIds = shopInfoDTO.getMarketplaceShopIdMap().keySet();
            // 请求全站点
            List<String> marketplaceIds = new ArrayList<>(shopInfoDTO.getMarketplaceShopIdMap().keySet());
            // 发起请求
            ApiResponse<GetOrdersResponse> ordersWithHttpInfo = api.getOrdersWithHttpInfo(marketplaceIds,
                    null, null, null, null, null, null, null, null, null, 100,
                    null, null, null, orderIds, null, null, null, null, null, null, null);
            List<String> limitArray = ordersWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetOrdersResponse orders = ordersWithHttpInfo.getData();

            List<Order> orderList = new LinkedList<>(orders.getPayload().getOrders());
            String currentNextToken = orders.getPayload().getNextToken();
            int currentSize = orders.getPayload().getOrders().size();
            while (StringUtils.isNotBlank(currentNextToken) && currentSize == 100) {
                GetOrdersResponse currentResp = api.getOrders(marketplaceIds, null, null, null, null, null, null, null, null, null, 100, null, null, currentNextToken, null, null, null, null, null, null, null, null);
                orderList.addAll(currentResp.getPayload().getOrders());
                // 亚马逊接口响应时间UTC转换8区
                currentNextToken = currentResp.getPayload().getNextToken();
                currentSize = currentResp.getPayload().getOrders().size();
                List<String> currentLimitArray = ordersWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                rateLimitStr = currentLimitArray.get(0);
            }
            // 返回下载源数据
            return orderList.stream()
                    .map(e-> new PlatformAmazonOrderDTO(e, shopInfoDTO, timeZoneList))
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            if (429 == e.getCode()){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            throw new ServiceException("根据订单IDS请求亚马逊SP-APi订单失败：API异常："+JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new RuntimeException("根据订单IDS请求亚马逊SP-APi订单失败,body=" + JSONUtil.toJsonStr(e));
        }
    }
}
