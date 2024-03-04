package com.erp.sdk.oms.amz.spapi.handler;

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
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfigurationOnRequests;
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.api.TokensApi;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_LIST;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX, data.getGroupId());
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);
        String rateLimitStr;
        // 根据亚马逊的响应时间记录下次执行开始时间
        LocalDateTime nextStartTime;

        // 亚马逊订单下载
        OrdersV0Api api = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
        // 正式环境请求
        // 东八区转UTC时间
        String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(data.getLastTime()).toString();
        String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(data.getNextTime()).toString();
        try {
            List<String> marketplaceIds = Collections.singletonList(marketPlaceEnum.getMarketplaceId());
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
                if (StringUtils.isNotBlank(rateLimitStr)){
                    RateLimitConfigurationOnRequests rateLimitConfigurationRequests = (RateLimitConfigurationOnRequests) rateLimitConfig;
                    rateLimitConfigurationRequests.setRateLimitPermit(Double.parseDouble(rateLimitStr));
                    api.getApiClient().setRateLimiter(rateLimitConfigurationRequests);
                }
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
            if (StringUtils.isNotBlank(rateLimitStr)){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN);
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            // 设置根据亚马逊的响应时间记录下次执行开始时间
            data.setNextTime(nextStartTime);
            // 返回下载源数据
            return orderList.stream()
                    .map(e-> new PlatformAmazonOrderDTO(e, shopInfoDTO.getId()))
                    .collect(Collectors.toList());
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
//        if (!CollectionUtils.isEmpty(dto.getDetails())){
//            // 已有信息不请求
//            log.info("亚马逊详情已有不请求, UniqueId={}", dto.getUniqueId());
//            return dto;
//        }
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS;
        // 默认请求速率配置
        String limitKey = extendObj.getString(AmazonRequestTypeRateLimiterEnum.limitKey);
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);
        String rateLimitStr;

        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = getAmazonShopInfoDTO(dto);
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 查询订单详情
        OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
        OrderItemList allOrderItems = null;
        try {
            ApiResponse<GetOrderItemsResponse> itemResponse = ordersVoApi.getOrderItemsWithHttpInfo(dto.getUniqueId(), null);
            List<String> limitArray = itemResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetOrderItemsResponse orderItems = itemResponse.getData();

            String currentNextToken = orderItems.getPayload().getNextToken();
            OrderItemList resultOrderItemsList = orderItems.getPayload().getOrderItems();
            while (StringUtils.isNotBlank(currentNextToken)) {
                ApiResponse<GetOrderItemsResponse> currentOrderItemsResp = ordersVoApi.getOrderItemsWithHttpInfo(dto.getUniqueId(), currentNextToken);
                GetOrderItemsResponse currentOrderItems = currentOrderItemsResp.getData();
                List<String> currentLimitArray = itemResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                rateLimitStr = currentLimitArray.get(0);
                resultOrderItemsList.addAll(currentOrderItems.getPayload().getOrderItems());
                currentNextToken = currentOrderItems.getPayload().getNextToken();
            }
            allOrderItems = resultOrderItemsList;

            if (StringUtils.isNotBlank(rateLimitStr)){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN);
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单详情失败："+JSONUtil.toJsonStr(e));
        }
        if (CollectionUtils.isEmpty(allOrderItems)){
            return dto;
        }
        dto.setDetails(allOrderItems);
        log.info("查询亚马逊订单详情成功, UniqueId={}", dto.getUniqueId());
        return dto;
    }

    /**
     * 请求RDT授权
     */
    private String queryAndGetRDT(PlatformAmazonOrderDTO dto, AmazonShopInfoDTO shopInfoDTO) {
        // 生成RDT权限获取地址信息
        // amazon-rdt-token:店铺ID:订单ID
        String tokenKey = StrUtil.format(RedisCacheConstants.AMAZON_RDT_TOKEN, dto.getShopId(), dto.getUniqueId());
        Object obj = redisUtil.get(tokenKey);
        // 当前RDT
        String rdtToken = "";
        if (null != obj){
            rdtToken = (String) obj;
        } else {
            AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            TokensApi api = TokensApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false);
            CreateRestrictedDataTokenRequest body = CreateRestrictedDataTokenRequest.builderByOrderId(dto.getUniqueId());
            try {
                CreateRestrictedDataTokenResponse response = api.createRestrictedDataToken(body);
                rdtToken = response.getRestrictedDataToken();
                // 缓存到redis
                redisUtil.set(tokenKey, rdtToken, response.getExpiresIn());
            } catch (Exception e) {
                throw new ServiceException("获取亚马逊订单RDT token失败："+JSONUtil.toJsonStr(e));
            }
        }
        if (StringUtils.isBlank(rdtToken)){
            throw new ServiceException("亚马逊RDT token异常：空："+JSONUtil.toJsonStr(dto));
        }
        return rdtToken;
    }


    @DataIdempotent(keyIdName = "dto.redissonKey", waitTime = 20)
    public PlatformAmazonOrderDTO downloadAddress(PlatformAmazonOrderDTO dto, JSONObject extendObj) {
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
        String limitKey = extendObj.getString(AmazonRequestTypeRateLimiterEnum.limitKey);
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS;
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);
        OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
        // 查询订单详情
        String rateLimitStr;

        // 生成RDT权限获取地址信息
        // amazon-rdt-token:店铺ID:订单ID
        String rdtToken = queryAndGetRDT(dto, shopInfoDTO);

        // 修改x-amz-access-token的token
        ordersVoApi.getApiClient().addDefaultHeader(ApiClient.SIGNED_ACCESS_TOKEN_HEADER_NAME, rdtToken);
        try {
            ApiResponse<GetOrderAddressResponse> orderAddressResp = ordersVoApi.getOrderAddressWithHttpInfo(dto.getUniqueId());
            List<String> limitArray = orderAddressResp.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetOrderAddressResponse response = orderAddressResp.getData();
            if (StringUtils.isNotBlank(rateLimitStr)){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN);
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            Address shippingAddress = response.getPayload().getShippingAddress();
            Order order = dto.getOrder();
            order.setShippingAddress(shippingAddress);
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单地址失败："+JSONUtil.toJsonStr(e));
        }
        return dto;
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

}
