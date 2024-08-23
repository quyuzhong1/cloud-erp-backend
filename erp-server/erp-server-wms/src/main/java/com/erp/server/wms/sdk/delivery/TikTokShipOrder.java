package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cSourcePlatformEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.ship.SelfShipmentBean;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOtherParam;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUS;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUSParam;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.TIK_TOK)
public class TikTokShipOrder extends AbstractShipOrder {

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        Tuple tuple = super.allSourceOrderInfo(dto);
        // 所有源单信息
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        // 对应明细
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);

        List<String> resultDetailIds = new ArrayList<>();

        for (SoB2cEntity entity : sourceOrderList) {
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityListMap.get(entity.getId());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            // 校验捆绑商品拆分
            // 来源明细ID为空代表是手工添加的明细忽略
            detailEntityList =  detailEntityList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSourceDetailId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("订单【{}】所有明细来源ID为空,不请求接口", entity.getCode());
                continue;
            }
            List<String> sourceDetailIds = detailEntityList.stream().map(SoB2cDetailEntity::getSourceDetailId).collect(Collectors.toList());
            List<String> detailIdList = detailEntityList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());

            TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(entity.getShopId());

            //获取销售渠道信息
            LogisticsChannelDTO.SignShipDTO tmsScaleChannelShipDTO = logisticsFeign.getScaleChannelByChannelById(
                    logisticsEntity.getLogisticsChannelId(),
                    PlatformDictEnum.TIK_TOK.getCode()
            );
            if (null == tmsScaleChannelShipDTO){
                throw new ServiceException("找不到渠道信息");
            }
            //获取渠道标发单号
            String standardOrderType = tmsScaleChannelShipDTO.checkAndGetOrderDeliveryMarkType();
            String trackingNumber = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(),standardOrderType)
                    ? logisticsEntity.getCode() : logisticsEntity.getTrackNo();
            if (StrUtil.isBlank(trackingNumber)) {
                throw new ServiceException("操作失败，渠道标发单号为空");
            }

            if ("US".equalsIgnoreCase(tikTokShopInfoDTO.getSite())) {
                ShipOrderUSParam paramDTO = new ShipOrderUSParam();
                paramDTO.setTrackingNumber(trackingNumber);
                paramDTO.setOrderLineItemIds(sourceDetailIds);
                paramDTO.setShippingProviderId(tmsScaleChannelShipDTO.getCode());
                ShipOrderUS shipOrderUS = tikTokSdkClientService.sendTikTokShipOrderUS(tikTokShopInfoDTO, entity.getPlatformCode(), paramDTO);
                resultDetailIds.addAll(detailIdList);
                if (shipOrderUS.getCode() != 0) {
                    if (!"Package has been shipped. Please not ship the package again.".equalsIgnoreCase(shipOrderUS.getMessage())
                            && !"fulfillment not allow forward".equalsIgnoreCase(shipOrderUS.getMessage())) {
                        throw new ServiceException(shipOrderUS.getMessage());
                    }
                }

            } else {
                for (SoB2cDetailEntity detailEntity : detailEntityList) {
                    ShipOrderOtherParam paramDTO = new ShipOrderOtherParam();
                    SelfShipmentBean selfShipmentBean = new SelfShipmentBean();
                    selfShipmentBean.setTrackingNumber(trackingNumber);
                    selfShipmentBean.setShippingProviderId(tmsScaleChannelShipDTO.getCode());
                    paramDTO.setSelfShipment(selfShipmentBean);
                    tikTokSdkClientService.sendTikTokShipOrderOther(tikTokShopInfoDTO, detailEntity.getPlatformPackageId(), paramDTO);
                }
                resultDetailIds.addAll(detailIdList);
            }
        }
        return resultDetailIds;
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
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList) {
        return null;
    }
}
