package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.convert.WalmartShipOrderConverter;
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
public class ShopifyShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private ShopifyRestClientService shopifyRestClientService;

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {
        //检查销售订单是否存在
        SoB2cEntity mainEntity = soB2cFeign.getById(dto.getSoB2cId());
        if (ObjectUtil.isEmpty(mainEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //检查销售订单物流信息是否存在
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(mainEntity.getId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsEntities.get(0);

        //检查销售订单详情是否存在
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(dto.getSoB2cId()));
        if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        if (soB2cDetailEntityList.stream().anyMatch(e-> StringUtils.isBlank(e.getSourceDetailId()))){
            throw new ServiceException("平台来源详情ID为空");
        }

        String shopId = mainEntity.getShopId();
        ShopifyShopInfoDTO shopInfoDTO = ShopSdkServer.getTokenAndDomainByShopId(shopId);
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
        if (CollectionUtils.isEmpty(fulfillmentOrdersFromOrderList)){
            throw new ServiceException("找不到Shopify发货单");
        }
        // 校验不为空
//        if (fulfillmentOrdersFromOrderList.stream().anyMatch(e-> CollectionUtils.isEmpty(e.getLineItems()))){
//            log.error("[Shopify标记发货]Shopify数据异常: json={}", JSONUtil.toJsonStr(fulfillmentOrdersFromOrderList));
//            throw new ServiceException("Shopify数据异常：详情LineItems为空");
//        }
//        // Map<lineItemId, ShopifyFulfillmentOrder>
//        Map<String, ShopifyFulfillmentOrder> fulfillmentOrderMap = fulfillmentOrdersFromOrderList.stream().collect(Collectors.toMap(e -> e.getLineItems().get(0).getLineItemId(), Function.identity()));
//
//        // 组合请求参数
//        List<ShopifyLineItemsByFulfillmentOrder> orderList = new LinkedList<>();
//        for (SoB2cDetailEntity detailEntity : soB2cDetailEntityList) {
//            ShopifyFulfillmentOrder fulfillmentOrder = fulfillmentOrderMap.get(detailEntity.getSourceDetailId());
//            if (null == fulfillmentOrder){
//                throw new ServiceException("未找到对应Shopify的发货配送信息：lineItemId=" + detailEntity.getSourceDetailId());
//            }
//            // 组合请求参数
//            ShopifyLineItemsByFulfillmentOrder orderRequestDTO = convertRequestOrderDTO(detailEntity, fulfillmentOrder);
//            orderList.add(orderRequestDTO);
//        }
//
//        ShopifyFulfillmentPayload payload = new ShopifyFulfillmentPayload();
//        ShopifyTrackingInfo trackingInfo = new ShopifyTrackingInfo();
//        trackingInfo.setNumber(logisticsEntity.getTrackNo());
//        trackingInfo.setUrl("");
//
//        payload.setLineItemsByFulfillmentOrder(orderList);
//        payload.setTrackingInfo(trackingInfo);
//        ShopifyFulfillmentPayloadRoot request = new ShopifyFulfillmentPayloadRoot();
//        request.setFulfillment(payload);
//        // Creates a fulfillment for one or many fulfillment orders
//        final ShopifyFulfillment actualShopifyFulfillment = shopifyRestClient.createFulfillment(request);
//        log.warn("[Shopify标记发货]创建Fulfillment结果：{}",JSONUtil.toJsonStr(actualShopifyFulfillment));
//        if (null == actualShopifyFulfillment){
//            throw new ServiceException("Shopify创建Fulfillment失败");
//        }
    }

    /**
     * 组合Order请求参数
     */
    private ShopifyLineItemsByFulfillmentOrder convertRequestOrderDTO(SoB2cDetailEntity detailEntity, ShopifyFulfillmentOrder fulfillmentOrder) {
        final String lineItemId = detailEntity.getSourceDetailId();
        final String fulfillmentOrderId = fulfillmentOrder.getId();
        final long quantity = detailEntity.getQty();
        ShopifyLineItemsByFulfillmentOrder order = new ShopifyLineItemsByFulfillmentOrder();
        order.setFulfillmentOrderId(fulfillmentOrderId);
        List<ShopifyFulfillmentOrderPayloadLineItem> items = new LinkedList<>();
        ShopifyFulfillmentOrderPayloadLineItem item = new ShopifyFulfillmentOrderPayloadLineItem();
        item.setQuantity(quantity);
        item.setId(lineItemId);
        items.add(item);
        order.setFulfillmentOrderLineItems(items);
        return order;
    }
}
