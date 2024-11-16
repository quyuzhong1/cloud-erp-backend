package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.*;
import com.erp.server.wms.service.DictBasicService;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.AMAZON)
public class AmazonShipOrder extends AbstractShipOrder {

    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;

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
            List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            // 校验捆绑商品拆分
            // 来源明细ID为空代表是手工添加的明细忽略
            detailEntityList =  detailEntityList.stream()
                    .filter(e -> CharSequenceUtil.isNotBlank(e.getSourceDetailId()))
                    .collect(Collectors.toList());
//            if (detailEntityList.stream().anyMatch(e -> CharSequenceUtil.isBlank(e.getSourceDetailId()))) {
//                throw new ServiceException("平台来源详情ID为空");
//            }
            detailEntityList = super.handleSplit(detailEntityList, dto.isFalseDeliveryFlag());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("订单【{}】所有明细来源ID为空,不请求亚马逊接口", mainEntity.getCode());
                continue;
            }

            //检查销售订单物流信息是否存在
            if (CharSequenceUtil.isBlank(logisticsEntity.getLogisticsChannelId())){
                throw new ServiceException("订单渠道ID为空");
            }
            //获取销售渠道信息
            LogisticsChannelDTO.SignShipDTO tmsScaleChannelShipDTO = logisticsFeign.getScaleChannelByChannelById(
                    logisticsEntity.getLogisticsChannelId(),
                    PlatformDictEnum.AMAZON.getCode()
            );
            if (null == tmsScaleChannelShipDTO){
                throw new ServiceException("找不到渠道信息");
            }
            // 获取店铺授权信息
            AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(mainEntity.getShopId());
            if (null == shopInfoDTO) {
                throw new ServiceException("未找到店铺授权:" + mainEntity.getShopId());
            }
            AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            OrdersV0Api api = OrdersV0Api.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

            //取消则需要自动发起订单拦截
            PlatformDeliveryInterceptDTO interceptDTO = PlatformDeliveryInterceptDTO.builder()
                    .soB2cId(mainEntity.getId())
                    .dictPlatform(mainEntity.getDictPlatform())
                    .oldIsCancel(mainEntity.getIsCancel())
                    .shopId(mainEntity.getShopId())
                    .platformCode(mainEntity.getPlatformCode())
                    .build();
            Boolean isCancel = deliveryIntercept(interceptDTO);
            if (isCancel){
                throw new ServiceException(CharSequenceUtil.format("销售订单【{}】平台已取消，不支持发货",mainEntity.getCode()));
            }

            ConfirmShipmentRequest body = new ConfirmShipmentRequest();
            body.setMarketplaceId(marketplaceEnum.getMarketplaceId());
            PackageDetail packageDetail = new PackageDetail();
            // 包裹参考 ID 支持任何正数值，用于在确认货件后编辑货件。您可以提交任何数值作为 packageReferenceID，
            // 我们将存储该数据。如果您需要对货件进行编辑，请使用相同的 packageReferenceID 提交另一个 confirmShipment 操作。提交成功后，其他货件详情将被编辑
            packageDetail.setPackageReferenceId("1");
            // 物流渠道代号
            packageDetail.setCarrierCode(tmsScaleChannelShipDTO.getCode());
            // 物流渠道名称
            packageDetail.setCarrierName(tmsScaleChannelShipDTO.getSaleChannelSupplierName());
            // 物流服务商=物流渠道名称
            packageDetail.setShippingMethod(tmsScaleChannelShipDTO.getSaleChannelSupplierName());

            //获取渠道标发单号
            String standardOrderType = tmsScaleChannelShipDTO.checkAndGetOrderDeliveryMarkType();
            String trackingNumber = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(),standardOrderType)
                    ? logisticsEntity.getCode() : logisticsEntity.getTrackNo();
            if (CharSequenceUtil.isBlank(trackingNumber)) {
                throw new ServiceException("操作失败，渠道标发单号为空");
            }
            // 物流运单号
            packageDetail.setTrackingNumber(trackingNumber);

            // 发货时间
            String shipDateTime = DateUtil.plus8SameUtcOffset(LocalDateTime.now()).toString();
            packageDetail.setShipDate(shipDateTime);
            // 组合item
            ConfirmShipmentOrderItemsList orderItemList = new ConfirmShipmentOrderItemsList();
            for (SoB2cDetailEntity detailEntity : detailEntityList) {
                ConfirmShipmentOrderItem orderItem = new ConfirmShipmentOrderItem();
                orderItem.setOrderItemId(detailEntity.getSourceDetailId());
                orderItem.setQuantity(detailEntity.getQty());
                orderItemList.add(orderItem);
            }
            packageDetail.setOrderItems(orderItemList);
            body.setPackageDetail(packageDetail);

            // 非线上环境需要指定订单ID
            if (!BusinessCommonConstants.hasProfile("prod")){
                List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("amazonAllowShipOrderId");
                if (CollectionUtils.isEmpty(warehouseTypes)){
                    log.warn("【{}】不存在指定的订单ID配置,不请求亚马逊接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(body));
                    signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                    continue;
                }
                // 允许通过的ID
                DictBasicDTO.ListDTO configAllowPlatformOrderDTO = warehouseTypes.stream().filter(e -> mainEntity.getPlatformCode().equalsIgnoreCase(e.getValue())).findFirst().orElse(null);
                if (null == configAllowPlatformOrderDTO){
                    log.warn("【{}】不属于配置指定的订单ID,不请求亚马逊接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(body));
                    signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                    continue;
                }
            }

            try {
                log.warn("【{}】亚马逊标记发货:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(body));
                ApiResponse<Void> voidApiResponse = api.confirmShipmentWithHttpInfo(body, mainEntity.getPlatformCode());
                log.warn("【{}】亚马逊标记发货:响应结果={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(voidApiResponse));
                signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            } catch (ApiException e){
                confirmShipmentApiExceptionHandle(e, mainEntity.getPlatformCode(), api);
            } catch (Exception e) {
                throw new ServiceException("亚马逊标记发货失败:" + e.getMessage());
            }
        }
        return signShippedDetailList;
    }


    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        Boolean isCancel = dto.getOldIsCancel();
        // 取消实时查询，调整到打印拣货单批量查询
//        if (!dto.getOldIsCancel()){
//            isCancel = queryAndUpdateOrderStatus(dto);
//        }

        if (isCancel) {
            //订单拦截
            soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(dto.getSoB2cId(), "平台取消或退款"));
        }
        return isCancel;
    }


    /**
     * 查询并更新平台订单状态
     */
    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        if (dto.getOldIsCancel()){
            return true;
        }
        Boolean isCancel;
        Stopwatch stopwatch = Stopwatch.createStarted();
        // 请求亚马逊接口获取最新状态
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(dto.getShopId());
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + dto.getShopId());
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        OrdersV0Api api = OrdersV0Api.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
        try {
            stopwatch.stop();
            log.warn("查询亚马逊订单状态初始话客户端结束，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
            stopwatch.reset();
            stopwatch.start();
            GetOrderResponse response = api.getOrder(dto.getPlatformCode());
            stopwatch.stop();
            log.warn("查询亚马逊订单状态结束，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
            isCancel = response.getPayload().convertCancel();
        } catch (Exception e) {
            log.warn("查询亚马逊订单【{}】信息响应结果: error={}", dto.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
            throw new ServiceException("查询亚马逊订单最新信息失败:" + e.getMessage());
        }
        // 订单取消:分事务标记到订单
        if (isCancel){
            dto.setOldIsCancel(isCancel);
            soB2cFeign.updateCancelAndLog(dto);
        }
        return isCancel;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList){
        List<String> shopIds = dtoList.stream().map(PlatformOrderQueryDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIds);
        if (CollectionUtils.isEmpty(shopInfoEntityList)){
            throw new ServiceException("未找到店铺ids=" + shopIds);
        }
        // 按亚马逊账号分组
        Map<String, List<ShopInfoEntity>> platformGroup = shopInfoEntityList
                .stream().
                collect(Collectors.groupingBy(ShopInfoEntity::getPlatformShopCode));
        // 异常按亚马逊账号查询
        platformGroup.entrySet().parallelStream().peek(e -> {
            // 当前账号的所有
            List<PlatformOrderQueryDTO> curDtoList = dtoList.stream()
                    .filter(dto -> shopIds.contains(dto.getShopId()))
                    .collect(Collectors.toList());
            List<List<PlatformOrderQueryDTO>> partition = Lists.partition(curDtoList, 50);
            for (List<PlatformOrderQueryDTO> queryDTOS : partition) {
                String shopId = queryDTOS.get(0).getShopId();
                List<String> platformCodeList = queryDTOS.stream().map(PlatformOrderQueryDTO::getPlatformCode).distinct().collect(Collectors.toList());
                // 请求亚马逊接口获取最新状态
                // 获取店铺授权信息
                AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
                if (null == shopInfoDTO) {
                    throw new ServiceException("未找到店铺授权:" + shopId);
                }
                AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
                OrdersV0Api api = OrdersV0Api.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
                // 站点信息
                List<String> marketplaceIds = e.getValue().stream()
                        .map(ShopInfoEntity::getDictCountryCode)
                        .map(AmazonMarketplaceEnum::getByCountryCode)
                        .map(AmazonMarketplaceEnum::getMarketplaceId)
                        .distinct()
                        .collect(Collectors.toList());
                try {
                    // 批量查询
                    ApiResponse<GetOrdersResponse> ordersWithHttpInfo = api.getOrdersWithHttpInfo(marketplaceIds,
                            null, null, null, null, null, null, null, null, null, 100,
                            null, null, null, platformCodeList, null, null, null, null, null, null, null);
                    // 过滤获取已取消的订单
                    List<String> cancelOrderCodeList = ordersWithHttpInfo.getData()
                            .getPayload()
                            .getOrders()
                            .stream()
                            .filter(Order::convertCancel)
                            .map(Order::getAmazonOrderId)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(cancelOrderCodeList)) {
                        continue;
                    }
                    List<String> soB2cIdList = queryDTOS.stream()
                            .filter(cur -> cancelOrderCodeList.contains(cur.getPlatformCode()))
                            .map(PlatformOrderQueryDTO::getSoB2cId)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(soB2cIdList)) {
                        continue;
                    }
                    soB2cFeign.batchUpdateCancelAndLog(soB2cIdList);
                } catch (Exception error) {
                    log.warn("查询亚马逊订单【{}】信息响应结果: error={}", platformCodeList, ExceptionUtil.stacktraceToString(error));
                    throw new ServiceException("查询亚马逊订单最新信息失败:" + error);
                }

            }
        }).collect(Collectors.toList())
        return true;
    }

    /**
     * 标记发货亚马逊Api异常处理
     */
    public void confirmShipmentApiExceptionHandle(ApiException e, String platformCode, OrdersV0Api api) {
        if (!CharSequenceUtil.isBlank(e.getMessage())){
            // 其他异常信息
            throw new ServiceException("亚马逊标记发货失败:" + e.getMessage());
        }
        JSONArray errorJsonArray = new JSONObject(e.getResponseBody()).getJSONArray("errors");
        if (CollectionUtils.isEmpty(errorJsonArray)){
            // 其他异常信息
            throw new ServiceException("亚马逊标记发货失败:" + JSONUtil.toJsonStr(e));
        }
//            { "errors": [{
//              "code": "InvalidInput",
//             "message": "Failed to create package due to not finding a matching package or order already fulfilled. ErrorMessage(errorCode\u003dGeneralError, errorDescription\u003dUnable to find package to update for order (250-6220485-3709425), errorMetric\u003dPackageToUpdateNotFound, packageIndex\u003dnull)",
//             "details": ""}]}
        JSONObject errorObj = errorJsonArray.getJSONObject(0);
        String errorMsg = errorObj.getStr("message");
        if (errorMsg.contains("ErrorCode: NonexistentOrderItem Description: Failed to find order item list by order ID:")){
            // 判断响应异常信息是取消订单
            throw new ServiceException("平台取消发货，不允许出库，请处理订单发货拦截后，取消发货");
        } else if (errorMsg.contains("Failed to create package due to not finding a matching package or order already fulfilled")){
            // 判断响应异常信息是后台可能已标记发货
            try {
                GetOrderResponse response = api.getOrder(platformCode);
                if (null == response.getPayload()){
                    throw new ServiceException("查询亚马逊订单最新信息为空:" + errorMsg);
                } else if (!"shipped".equals(response.getPayload().convertBillStatus())){
                    throw new ServiceException("亚马逊非已发货-标记发货失败:" + errorMsg);
                }
                //平台已发货 跳过
                log.warn("亚马逊订单【{}】标记发货:亚马逊订单已发货忽略", platformCode);
            } catch (Exception apiError) {
                log.warn("查询亚马逊订单【{}】信息响应结果: error={}", platformCode, ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("查询亚马逊订单最新信息失败:" + errorMsg);
            }
        } else {
            // 其他异常信息
            throw new ServiceException("亚马逊标记发货失败:" + errorMsg);
        }
    }
}
