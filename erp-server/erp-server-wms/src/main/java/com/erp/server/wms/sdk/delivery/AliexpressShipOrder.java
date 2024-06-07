package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.exceptions.ExceptionUtil;
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
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.LogisticsMappingFeign;
import com.erp.server.wms.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void shipOrder(PlatformShipOrderDTO dto) {
        Tuple tuple = super.allSourceOrderInfo(dto);
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        Map<String, SoB2cLogisticsEntity> logisticsEntityMap = tuple.get(2);

        for (SoB2cEntity mainEntity : sourceOrderList) {
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            // 校验捆绑商品拆分
            // 来源明细ID为空代表是手工添加的明细忽略
            detailEntityList = detailEntityList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSourceDetailId()))
                    .collect(Collectors.toList());
//            if (detailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
//                throw new ServiceException("平台来源详情ID为空");
//            }
            detailEntityList = super.handleBomSplit(detailEntityList);
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("订单【{}】所有明细来源ID为空,不请求亚马逊接口", mainEntity.getCode());
                continue;
            }

            String soB2cId = dto.getSoB2cId();
            SoB2cDTO.SignShipOrderDTO signShipOrderDTO = soB2cFeign.getSignShipParam(soB2cId);
            //渠道
            String channelId=signShipOrderDTO.getLogisticsChannelId();

            //获取销售渠道信息
            LogisticsChannelDTO.SignShipDTO tmsSignShipDTO = logisticsFeign.getScaleChannelByChannelById(
                    channelId,
                    PlatformDictEnum.ALI_EXPRESS.getCode()
            );
            if (null == tmsSignShipDTO) {
                throw new ServiceException("找不到渠道信息");
            }

            //获取渠道标发单号
            String standardOrderType = tmsSignShipDTO.checkAndGetOrderDeliveryMarkType();
            String logisticsNo = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(), standardOrderType)
                    ? signShipOrderDTO.getLogisticsTransportNo() : signShipOrderDTO.getLogisticsTrackNo();
            if (StrUtil.isBlank(logisticsNo)) {
                throw new ServiceException("操作失败，渠道标发单号为空");
            }

            DeclareDeliverRequest request = DeclareDeliverRequest.builder().
                    outRef(signShipOrderDTO.getPlatformCode()).
                    logisticsNo(logisticsNo).
                    shopId(signShipOrderDTO.getShopId()).
                    shopName(signShipOrderDTO.getShopName()).
                    serviceName(tmsSignShipDTO.getSaleChannelSupplierName()).
                    build();

            // 非线上环境需要指定订单ID
            if (!BusinessCommonConstants.hasProfile("prod")) {
                List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("aliexpressAllowShipOrderId");
                if (CollectionUtils.isEmpty(warehouseTypes)) {
                    log.warn("【{}】不存在指定的订单ID配置,不请求速卖通接口:请求参数={}", signShipOrderDTO.getPlatformCode(), JSONUtil.toJsonStr(request));
                    return;
                }
                // 允许通过的ID
                DictBasicDTO.ListDTO configAllowPlatformOrderDTO = warehouseTypes.stream().filter(e -> signShipOrderDTO.getPlatformCode().equalsIgnoreCase(e.getValue())).findFirst().orElse(null);
                if (null == configAllowPlatformOrderDTO) {
                    log.warn("【{}】不属于配置指定的订单ID,不请求速卖通接口:请求参数={}", signShipOrderDTO.getPlatformCode(), JSONUtil.toJsonStr(request));
                    return;
                }
            }

            try {
                aliExpressOrderService.declareDeliver(request);
            } catch (ApiException e) {
                log.error("【{}】速卖通标记发货失败 >>>>{}", soB2cId, ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("速卖通标记发货失败:" + e.getMessage());
            }
        }
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
}
