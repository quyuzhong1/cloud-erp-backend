package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
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
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.api.TokensApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.*;
import com.erp.sdk.oms.amz.spapi.model.tokens.CreateRestrictedDataTokenRequest;
import com.erp.sdk.oms.amz.spapi.model.tokens.CreateRestrictedDataTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
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

    private static final String SIGNED_ACCESS_TOKEN_HEADER_NAME = "x-amz-access-token";

    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private RedisUtil redisUtil;

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

        // 亚马逊订单下载
        OrdersV0Api api = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false);
        String createdAfter = null;
        // 正式环境请求
        // 东八区转UTC时间
        String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(data.getLastTime()).toString();
        String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(data.getNextTime()).toString();
        try {
            // 发起请求
            List<Order> orderList = api.getAllOrders(Collections.singletonList(marketPlaceEnum.getMarketplaceId()),
                    createdAfter, null, lastUpdatedAfter, lastUpdatedBefore, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,null, null);
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
    public PlatformAmazonOrderDTO downloadDetail(PlatformAmazonOrderDTO dto, JSONObject extendObj) {
        if (StringUtils.isNotBlank(dto.getOrder().getShippingAddress().getName()) && !CollectionUtils.isEmpty(dto.getDetails())){
            // 已有信息不请求
            log.info("亚马逊详情和地址已有不请求, UniqueId={}", dto.getUniqueId());
            return dto;
        }
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(dto.getShopId());
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + dto.getShopId());
        }
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 查询订单详情
        OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false);
        OrderItemList allOrderItems = null;
        try {
            allOrderItems = ordersVoApi.getAllOrderItems(dto.getUniqueId(), null);
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单详情失败："+JSONUtil.toJsonStr(e));
        }
        if (CollectionUtils.isEmpty(allOrderItems)){
            return dto;
        }
        dto.setDetails(allOrderItems);
        log.info("查询亚马逊订单详情成功, UniqueId={}", dto.getUniqueId());
        if (StringUtils.isNotBlank(dto.getOrder().getShippingAddress().getName())){
            // 已有信息不请求
            log.info("亚马逊地址详情信息已有不请求, UniqueId={}", dto.getUniqueId());
            return dto;
        }
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

        // 修改x-amz-access-token的token
        ordersVoApi.getApiClient().addDefaultHeader(SIGNED_ACCESS_TOKEN_HEADER_NAME, rdtToken);
        try {
            GetOrderAddressResponse response = ordersVoApi.getOrderAddress(dto.getUniqueId());
            Address shippingAddress = response.getPayload().getShippingAddress();
            Order order = dto.getOrder();
            order.setShippingAddress(shippingAddress);
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单地址失败："+JSONUtil.toJsonStr(e));
        }
        return dto;
    }

}
