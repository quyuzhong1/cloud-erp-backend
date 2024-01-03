package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
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
import org.checkerframework.checker.units.qual.A;
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
        List<SoB2cEntity> sourceOrderList;
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = new HashMap<>();
        Map<String, SoB2cLogisticsEntity> logisticsEntityMap= new HashMap<>();

        // 查询合并来源关系
        List<SoB2cRefEntity> refEntityList = soB2cFeign.findMergeByTargetId(dto.getSoB2cId());
        if (CollectionUtils.isEmpty(refEntityList)){
            // 无合并
            //检查销售订单是否存在
            SoB2cEntity mainEntity = soB2cFeign.getById(dto.getSoB2cId());
            if (ObjectUtil.isEmpty(mainEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
            }
            sourceOrderList = Collections.singletonList(mainEntity);
            //检查销售订单物流信息是否存在
            List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(mainEntity.getId()));
            if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            logisticsEntityMap.put(dto.getSoB2cId(), soB2cLogisticsEntities.get(0));
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(dto.getSoB2cId()));
            if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            soB2cDetailEntityListMap.put(dto.getSoB2cId(),soB2cDetailEntityList);
        } else {
            // 有合并
            List<String> mainIds = refEntityList.stream().map(SoB2cRefEntity::getSourceId).distinct().collect(Collectors.toList());
            List<String> detailIds = refEntityList.stream().map(SoB2cRefEntity::getSourceDetailId).distinct().collect(Collectors.toList());
            sourceOrderList = soB2cFeign.listByIds(mainIds);
            //检查销售订单是否存在
            if (CollectionUtils.isEmpty(sourceOrderList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
            }
            sourceOrderList = sourceOrderList.stream()
                    .filter(e-> SourceTypeEnum.SO_B2C.getCode().equalsIgnoreCase(e.getSourceType()) && PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(e.getDictPlatform()))
                    .collect(Collectors.toList());
            //检查销售订单物流信息是否存在
            List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(mainIds);
            if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            logisticsEntityMap = soB2cLogisticsEntities.stream().collect(Collectors.toMap(SoB2cLogisticsEntity::getMainId, Function.identity()));
            // 查询所有明细
            List<SoB2cDetailEntity> allDetailList = soB2cFeign.listDetailByIds(detailIds);
            soB2cDetailEntityListMap = allDetailList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId));
        }


        for (SoB2cEntity mainEntity : sourceOrderList) {
            //检查销售订单物流信息是否存在
            SoB2cLogisticsEntity logisticsEntity = logisticsEntityMap.get(mainEntity.getId());
            if (null == logisticsEntity) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            if (soB2cDetailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
                throw new ServiceException("平台来源详情ID为空");
            }
            Map<String, SoB2cDetailEntity> detailEntityMap = soB2cDetailEntityList.stream().collect(Collectors.toMap(SoB2cDetailEntity::getSourceDetailId, Function.identity()));

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
            if (CollectionUtils.isEmpty(fulfillmentOrdersFromOrderList)) {
                throw new ServiceException("找不到Shopify发货单");
            }
            // 未签收的单
            fulfillmentOrdersFromOrderList = fulfillmentOrdersFromOrderList.stream().filter(e -> e.getStatus().equalsIgnoreCase("open")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(fulfillmentOrdersFromOrderList)) {
                log.warn("Shopify 忽略表发货, 订单已标记, platformCode={}, fulfillment={}", platformOrderId, JSONUtil.toJsonStr(fulfillmentOrdersFromOrderList));
                return;
            }

            // 校验不为空
            if (fulfillmentOrdersFromOrderList.stream().anyMatch(e -> CollectionUtils.isEmpty(e.getLineItems()))) {
                log.error("[Shopify标记发货]Shopify数据异常: json={}", JSONUtil.toJsonStr(fulfillmentOrdersFromOrderList));
                throw new ServiceException("Shopify数据异常：详情LineItems为空");
            }


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
                }
                orderRequestDTO.setFulfillmentOrderId(fulfillmentOrder.getId());
                orderRequestDTO.setFulfillmentOrderLineItems(items);
                orderList.add(orderRequestDTO);

                ShopifyFulfillmentPayload payload = new ShopifyFulfillmentPayload();
                ShopifyTrackingInfo trackingInfo = new ShopifyTrackingInfo();
                trackingInfo.setNumber(logisticsEntity.getTrackNo());
                trackingInfo.setUrl("");
                trackingInfo.setCompany(logisticsEntity.getCode());
                payload.setLineItemsByFulfillmentOrder(orderList);
                payload.setTrackingInfo(trackingInfo);
                ShopifyFulfillmentPayloadRoot request = new ShopifyFulfillmentPayloadRoot();
                request.setFulfillment(payload);
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
}
