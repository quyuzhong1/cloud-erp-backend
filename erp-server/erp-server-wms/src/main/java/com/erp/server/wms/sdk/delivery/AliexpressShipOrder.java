package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.ALI_EXPRESS)
public class AliexpressShipOrder extends AbstractShipOrder {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Resource
    private DictBasicService dictBasicService;

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        // 查询拆分前的原平台订单
//        dto.setHasFindSourcePlatformOrder(true);
        // 查询所有信息
        Tuple tuple = super.allSourceOrderInfo(dto);
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);
        // 源平台仓单信息
//        Map<String, List<SoB2cDetailEntity>> sourceDetailEntityMap = tuple.get(3);

        //渠道
        String channelId = logisticsEntity.getLogisticsChannelId();

        //获取销售渠道信息
        LogisticsChannelDTO.SignShipDTO tmsSignShipDTO = logisticsFeign.getScaleChannelByChannelById(
                channelId,
                PlatformDictEnum.ALI_EXPRESS.getCode()
        );
        if (null == tmsSignShipDTO) {
            throw new ServiceException("找不到渠道信息");
        }

        List<String> signShippedDetailList = new ArrayList<>();
        for (SoB2cEntity mainEntity : sourceOrderList) {
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> currentDetailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(currentDetailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            // 校验捆绑商品拆分
            // 来源明细ID为空代表是手工添加的明细忽略
            currentDetailEntityList = currentDetailEntityList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSourceDetailId()))
                    .collect(Collectors.toList());
//            if (detailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
//                throw new ServiceException("平台来源详情ID为空");
//            }
            List<SoB2cDetailEntity> detailEntityList = super.handleSplit(currentDetailEntityList, dto.isFalseDeliveryFlag());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("【速卖通标记发货】订单【{}】所有明细来源ID为空,不请求速卖通接口", mainEntity.getCode());
                continue;
            }

            //获取渠道标发单号
            String standardOrderType = tmsSignShipDTO.checkAndGetOrderDeliveryMarkType();
            String logisticsNo = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(), standardOrderType)
                    ? logisticsEntity.getCode() : logisticsEntity.getTrackNo();
            if (StrUtil.isBlank(logisticsNo)) {
                throw new ServiceException("【速卖通标记发货】操作失败，渠道标发单号为空");
            }
            // 源单信息明细
//            List<SoB2cDetailEntity> allSourceDetailEntityList = sourceDetailEntityMap.get(mainEntity.getPlatformCode());
//            if (CollectionUtils.isEmpty(allSourceDetailEntityList)){
//                throw new ServiceException("【速卖通标记发货】操作失败，原平台明细为空,平台单号=" + mainEntity.getCode());
//            }
            // 声明发货类型，all表示全部发货，part表示部分声明发货。
//            String sendType = convertSendType(detailEntityList, allSourceDetailEntityList);
            // 都是全部发货
            String sendType = "all";

            DeclareDeliverRequest request = DeclareDeliverRequest.builder()
                    .outRef(mainEntity.getPlatformCode())
                    .logisticsNo(logisticsNo)
                    .shopId(mainEntity.getShopId())
                    .shopName(mainEntity.getShopName())
                    .serviceName(tmsSignShipDTO.getCode())
                    .sendType(sendType)
                    .actualCarrier(tmsSignShipDTO.getCarrierCode())
                    .trackingWebSite(tmsSignShipDTO.getLogisticsTrackUrl())
                    .build();

            // 非线上环境需要指定订单ID
            if (!BusinessCommonConstants.hasProfile("prod")) {
                List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("aliexpressAllowShipOrderId");
                if (CollectionUtils.isEmpty(warehouseTypes)) {
                    log.warn("【速卖通标记发货】【{}】不存在指定的订单ID配置,不请求速卖通接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(request));
                    signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                    continue;
                }
                // 允许通过的ID
                DictBasicDTO.ListDTO configAllowPlatformOrderDTO = warehouseTypes.stream().filter(e -> mainEntity.getPlatformCode().equalsIgnoreCase(e.getValue())).findFirst().orElse(null);
                if (null == configAllowPlatformOrderDTO) {
                    log.warn("【速卖通标记发货】【{}】不属于配置指定的订单ID,不请求速卖通接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(request));
                    signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                    continue;
                }
            }

            try {
                aliExpressOrderService.subDeclareDeliver(request);
                signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            } catch (ApiException e) {
                log.error("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货失败 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("速卖通标记发货失败:" + e.getMessage());
            }
        }
        return signShippedDetailList;
    }

    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        Boolean isCancel = dto.getOldIsCancel();
        if (isCancel) {
            //订单拦截
            soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(dto.getSoB2cId(), "平台取消或退款"));
        }
        return isCancel;
    }


    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList) {
        return false;
    }


    /**
     * 检查和转换发货类型
     */
    private String convertSendType(List<SoB2cDetailEntity> detailEntityList, List<SoB2cDetailEntity> allSourceDetailEntityList) {
        // 多个明细一定是部分发货
        if (allSourceDetailEntityList.size() > 1) {
            return "part";
        }
        Map<String, Integer> shipMap = detailEntityList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getSourceDetailId()))
                .collect(Collectors.toMap(SoB2cDetailEntity::getSourceDetailId, SoB2cDetailEntity::getQty));

        // 声明发货类型，all表示全部发货，part表示部分声明发货
        boolean allShip = allSourceDetailEntityList.stream().allMatch(
                e -> Objects.equals(e.getQty(), shipMap.getOrDefault(e.getSourceDetailId(), 0)));

        return allShip ? "all" : "part";
    }
}
