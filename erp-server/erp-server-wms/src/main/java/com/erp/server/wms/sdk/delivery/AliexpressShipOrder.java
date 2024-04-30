package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.LogisticsMappingFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.ALI_EXPRESS)
public class AliexpressShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Resource
    private LogisticsMappingFeign logisticsMappingFeign;

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {
        String soB2cId = dto.getSoB2cId();
        try {
            SoB2cDTO.SignShipOrderDTO signShipOrderDTO = soB2cFeign.getSignShipParam(soB2cId);
            //渠道
            String channelId=signShipOrderDTO.getLogisticsChannelId();
            //获取渠道信息
            LogisticsChannelDTO.SignShipDTO tmsSignShipDTO = logisticsFeign.getSignShipInfoByChannelById(channelId);

            //获取渠道标发单号
            String standardOrderType = tmsSignShipDTO.checkAndGetOrderDeliveryMarkType();
            String logisticsNo = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(),standardOrderType)
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
            aliExpressOrderService.declareDeliver(request);
        } catch (ApiException e) {
           log.error("销售订单【{}】速卖通 标记发货失败 >>>>{}",soB2cId, ExceptionUtil.stacktraceToString(e));
        }


    }

    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        return null;
    }
}
