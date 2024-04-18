package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.AMAZON)
public class AmazonShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private LogisticsFeign logisticsFeign;

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
                    .filter(e-> SourceTypeEnum.SO_B2C.getCode().equalsIgnoreCase(e.getSourceType()) && PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(e.getDictPlatform()))
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
            // 非线上环境需要指定订单ID
            if (!BusinessCommonConstants.hasProfile("prod")){
                List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("amazonAllowShipOrderId");
                if (CollectionUtils.isEmpty(warehouseTypes)){
                    log.warn("【{}】不存在指定的订单ID配置,不请求亚马逊接口", mainEntity.getPlatformCode());
                    return;
                }
                // 允许通过的ID
                DictBasicDTO.ListDTO configAllowPlatformOrderDTO = warehouseTypes.stream().filter(e -> mainEntity.getPlatformCode().equalsIgnoreCase(e.getValue())).findFirst().orElse(null);
                if (null == configAllowPlatformOrderDTO){
                    log.warn("【{}】不属于配置指定的订单ID,不请求亚马逊接口", mainEntity.getPlatformCode());
                    return;
                }
            }
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            if (detailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
                throw new ServiceException("平台来源详情ID为空");
            }

            //检查销售订单物流信息是否存在
            SoB2cLogisticsEntity logisticsEntity = logisticsEntityMap.get(mainEntity.getId());
            if (null == logisticsEntity) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            if (StringUtils.isBlank(logisticsEntity.getLogisticsChannelId())){
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

            ConfirmShipmentRequest body = new ConfirmShipmentRequest();
            AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
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
            // 物流运单号
            packageDetail.setTrackingNumber(logisticsEntity.getCode());

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

            OrdersV0Api api = OrdersV0Api.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

            try {
                ApiResponse<Void> voidApiResponse = api.confirmShipmentWithHttpInfo(body, mainEntity.getPlatformCode());
                log.warn("标记发货响应结果:{}", JSONUtil.toJsonStr(voidApiResponse));
            } catch (ApiException e){
                if (e.getMessage().contains("ErrorCode: NonexistentOrderItem Description: Failed to find order item list by order ID:")){
                    throw new ServiceException("平台取消发货，不允许出库，请处理订单发货拦截后，取消发货");
                } else {
                    throw new ServiceException("亚马逊标记发货失败:" + e.getMessage());
                }
            } catch (Exception e) {
                throw new ServiceException("亚马逊标记发货失败:" + e.getMessage());
            }
        }
    }
}
