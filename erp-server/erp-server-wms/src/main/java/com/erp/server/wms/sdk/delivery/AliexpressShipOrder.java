package com.erp.server.wms.sdk.delivery;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
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

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {
        String soB2cId = dto.getSoB2cId();
        try {
            SoB2cDTO.SignShipOrderDTO signShipOrderDTO = soB2cFeign.getSignShipParam(soB2cId);
            //渠道
            String channelId=signShipOrderDTO.getLogisticsChannelId();
            //获取渠道信息
            LogisticsChannelDTO.SignShipDTO tmsSignShipDTO = logisticsFeign.getSignShipInfoByChannelById(channelId);

            DeclareDeliverRequest request = DeclareDeliverRequest.builder().
                    outRef(signShipOrderDTO.getPlatformCode()).
                    logisticsNo(signShipOrderDTO.getLogisticsTransportNo()).
                    shopId(signShipOrderDTO.getShopId()).
                    shopName(signShipOrderDTO.getShopName()).
                    serviceName(tmsSignShipDTO.getSaleChannelSupplierName()).
                    build();
            aliExpressOrderService.declareDeliver(request);
        } catch (ApiException e) {
           log.error("销售订单【{}】速卖通 标记发货失败 >>>>{}",soB2cId,e.getMessage());
        }


    }
}
