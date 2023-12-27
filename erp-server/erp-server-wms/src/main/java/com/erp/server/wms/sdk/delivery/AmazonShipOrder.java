package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        //检查销售订单是否存在
        SoB2cEntity mainEntity = soB2cFeign.getById(dto.getSoB2cId());
        if (ObjectUtil.isEmpty(mainEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
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
        List<SoB2cDetailEntity> detailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(mainEntity.getId()));
        if (CollectionUtils.isEmpty(detailEntityList)){
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //检查销售订单物流信息是否存在
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(mainEntity.getId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsEntities.get(0);
        if (StringUtils.isBlank(logisticsEntity.getLogisticsChannelId())){
            throw new ServiceException("订单渠道ID为空");
        }
        //获取渠道信息
        LogisticsChannelDTO.SignShipDTO tmsSignShipDTO = logisticsFeign.getSignShipInfoByChannelById(logisticsEntity.getLogisticsChannelId());
        if (null == tmsSignShipDTO){
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
        packageDetail.setPackageReferenceId(logisticsEntity.getId());
        packageDetail.setCarrierCode(tmsSignShipDTO.getCode());
        packageDetail.setTrackingNumber(logisticsEntity.getTrackNo());
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
        OrdersV0Api api = OrdersV0Api.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false);

        try {
            ApiResponse<Void> voidApiResponse = api.confirmShipmentWithHttpInfo(body, mainEntity.getPlatformCode());
            log.warn("标记发货响应结果:{}", JSONUtil.toJsonStr(voidApiResponse));
        } catch (Exception e) {
            throw new ServiceException("亚马逊标记发货失败:" + e.getMessage());
        }

    }
}
