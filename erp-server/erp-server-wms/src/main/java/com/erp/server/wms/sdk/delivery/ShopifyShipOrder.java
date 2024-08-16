package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.LogisticsMappingFeign;
import com.sdk.oms.shopify.api.rest.ShopifyRestClient;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.*;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.SHOPIFY)
public class ShopifyShipOrder extends AbstractShipOrder {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsMappingFeign logisticsMappingFeign;

    @Resource
    private ShopifyRestClientService shopifyRestClientService;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private ShopSdkServer shopSdkServer;

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        Tuple tuple = super.allSourceOrderInfo(dto);
        // 所有源单信息
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        // 对应明细
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);

        List<String> signShippedDetailList = new ArrayList<>();
        for (SoB2cEntity mainEntity : sourceOrderList) {
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            // 来源明细ID为空代表是手工添加的明细忽略
            soB2cDetailEntityList =  soB2cDetailEntityList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSourceDetailId()))
                    .collect(Collectors.toList());
//            if (detailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
//                throw new ServiceException("平台来源详情ID为空");
//            }
            if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
                log.warn("订单【{}】所有明细来源ID为空,不请求接口", mainEntity.getCode());
                continue;
            }
            if (soB2cDetailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
                throw new ServiceException("平台来源详情ID为空");
            }
            soB2cDetailEntityList = super.handleSplit(soB2cDetailEntityList, dto.isFalseDeliveryFlag());
            if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
                log.warn("订单【{}】所有明细来源ID为空,不请求shopify接口", mainEntity.getCode());
                continue;
            }
            Map<String, SoB2cDetailEntity> detailEntityMap = soB2cDetailEntityList.stream().collect(Collectors.toMap(SoB2cDetailEntity::getSourceDetailId, Function.identity()));
            log.warn("[Shopify标记发货] 平台订单号【{}】,当前提交明细IDS:{}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(detailEntityMap.keySet()));
            String shopId = mainEntity.getShopId();
            ShopifyShopInfoDTO shopInfoDTO = shopSdkServer.getTokenAndDomainByShopId(shopId);
            if (null == shopInfoDTO) {
                log.error("[Shopify标记发货]从缓存中获取shopify token 失败: shopId={}", shopId);
                throw new ServiceException();
            }
            // 请求相关信息
            String platformOrderId = mainEntity.getPlatformCode();
            String shopifyShopDomain = shopInfoDTO.getShopDomain();
            String accessToken = shopInfoDTO.getAccessToken();
            // 初始化客户端
            ShopifyRestClient shopifyRestClient = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken);
            // Retrieves a list of fulfillment orders for a specific order
            List<ShopifyFulfillmentOrder> fulfillmentOrdersFromOrderList = shopifyRestClient.getFulfillmentOrdersFromOrder(platformOrderId);
            log.warn("[Shopify标记发货] 订单ID={}, 获取的配送明细参数 fulfillmentOrdersFromOrderList={}",platformOrderId, JSONUtil.toJsonStr(fulfillmentOrdersFromOrderList));
            if (CollectionUtils.isEmpty(fulfillmentOrdersFromOrderList)) {
                throw new ServiceException("找不到Shopify发货单");
            }
            // 未签收的单
            fulfillmentOrdersFromOrderList = fulfillmentOrdersFromOrderList.stream()
                    .filter(e -> e.getStatus().equalsIgnoreCase("open") || "in_progress".equalsIgnoreCase(e.getStatus()) )
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(fulfillmentOrdersFromOrderList)) {
                log.warn("Shopify 忽略表发货, 订单已标记, platformCode={}, fulfillment={}", platformOrderId, JSONUtil.toJsonStr(fulfillmentOrdersFromOrderList));
                continue;
            }

            // 校验不为空
            if (fulfillmentOrdersFromOrderList.stream().anyMatch(e -> CollectionUtils.isEmpty(e.getLineItems()))) {
                log.error("[Shopify标记发货]Shopify数据异常: json={}", JSONUtil.toJsonStr(fulfillmentOrdersFromOrderList));
                throw new ServiceException("Shopify数据异常：详情LineItems为空");
            }

            //获取销售渠道信息
            LogisticsChannelDTO.SignShipDTO tmsScaleChannelShipDTO = logisticsFeign.getScaleChannelByChannelById(
                    logisticsEntity.getLogisticsChannelId(),
                    PlatformDictEnum.SHOPIFY.getCode()
            );


            // 需要根据配送服务分组请求参数
            for (ShopifyFulfillmentOrder fulfillmentOrder : fulfillmentOrdersFromOrderList) {
                // 组合请求参数
                List<ShopifyLineItemsByFulfillmentOrder> orderList = new LinkedList<>();
                List<ShopifyFulfillmentOrderPayloadLineItem> items = new LinkedList<>();
                // 组合请求参数
                ShopifyLineItemsByFulfillmentOrder orderRequestDTO = new ShopifyLineItemsByFulfillmentOrder();
                for (ShopifyFulfillmentOrderLineItem lineItem : fulfillmentOrder.getLineItems()) {
                    SoB2cDetailEntity detailEntity = detailEntityMap.get(lineItem.getLineItemId());
                    if (null == detailEntity) {
                        continue;
                    }
                    ShopifyFulfillmentOrderPayloadLineItem item = new ShopifyFulfillmentOrderPayloadLineItem();
                    item.setQuantity(detailEntity.getQty());
                    item.setId(lineItem.getId());
                    items.add(item);
                    signShippedDetailList.add(detailEntity.getId());
                }
                orderRequestDTO.setFulfillmentOrderId(fulfillmentOrder.getId());
                orderRequestDTO.setFulfillmentOrderLineItems(items);
                orderList.add(orderRequestDTO);

                ShopifyFulfillmentPayload payload = new ShopifyFulfillmentPayload();
                ShopifyTrackingInfo trackingInfo = new ShopifyTrackingInfo();

                //获取渠道标发单号
                String standardOrderType = tmsScaleChannelShipDTO.checkAndGetOrderDeliveryMarkType();
                String trackingNumber = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(),standardOrderType)
                        ? logisticsEntity.getCode() : logisticsEntity.getTrackNo();
                if (StrUtil.isBlank(trackingNumber)) {
                    throw new ServiceException("操作失败，渠道标发单号为空");
                }

                trackingInfo.setNumber(trackingNumber);
                trackingInfo.setUrl("");
                trackingInfo.setCompany(tmsScaleChannelShipDTO.getCode());
                payload.setLineItemsByFulfillmentOrder(orderList);
                payload.setTrackingInfo(trackingInfo);
                ShopifyFulfillmentPayloadRoot request = new ShopifyFulfillmentPayloadRoot();
                request.setFulfillment(payload);
                // 查询订单发货状态
                ShopifyOrder shopifyOrder = shopifyRestClient.getOrder(platformOrderId);
                if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equalsIgnoreCase(shopifyOrder.convertBillStatus())){
                    log.warn("[Shopify标记发货] platformCode={},平台订单已发货跳过：,dto={}", platformOrderId, JSONUtil.toJsonStr(request));
                    continue;
                }

                // 在非正式环境
                if (!BusinessCommonConstants.hasProfile("prod")){
                    // 在非正式环境，店铺域名带test允许触发平台标记发货
                    if (shopifyShopDomain.contains("test")){
                        log.warn("[Shopify测试账号触发标记发货] platformCode={},创建Fulfillment参数：,dto={}", platformOrderId, JSONUtil.toJsonStr(request));
                        final ShopifyFulfillment actualShopifyFulfillment = shopifyRestClient.createFulfillment(request);
                        log.warn("[Shopify测试账号触发标记发货] platformCode={},创建Fulfillment结果：{}", platformOrderId, JSONUtil.toJsonStr(actualShopifyFulfillment));
                    } else {
                        log.warn("【{}】非正式环境不带test域名的店铺：不请求Shopify接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(request));
                    }
                }else{
                    log.warn("[Shopify标记发货]platformCode={},创建Fulfillment参数：,dto={}", platformOrderId, JSONUtil.toJsonStr(request));
                    // Creates a fulfillment for one or many fulfillment orders
                    final ShopifyFulfillment actualShopifyFulfillment = shopifyRestClient.createFulfillment(request);
                    log.warn("[Shopify标记发货] platformCode={},创建Fulfillment结果：{}", platformOrderId, JSONUtil.toJsonStr(actualShopifyFulfillment));
                    if (null == actualShopifyFulfillment) {
                        throw new ServiceException("Shopify创建Fulfillment失败");
                    }
                }
            }
        }
        return signShippedDetailList;
    }


    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        return null;
    }


    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList){
        return null;
    }
}
