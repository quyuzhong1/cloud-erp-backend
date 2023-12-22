package com.erp.server.wms.sdk.delivery;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.oms.feign.SoB2cFeign;
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
    private AliExpressOrderService aliExpressOrderService;

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {
        String soB2cId = dto.getSoB2cId();
        if (StringUtils.isBlank(soB2cId)) {
            return;
        }
        SoB2cDTO.SignShipOrderDTO signShipOrderDTO = soB2cFeign.getSignShipParam(soB2cId);
        DeclareDeliverRequest request=DeclareDeliverRequest.builder().
                outRef(signShipOrderDTO.getPlatformCode()).
                logisticsNo(signShipOrderDTO.getLogisticsTransportNo()).
                shopId(signShipOrderDTO.getShopId()).
                shopName(signShipOrderDTO.getShopName()).
                build();
        try {
            aliExpressOrderService.declareDeliver(request);
        } catch (ApiException e) {
            e.printStackTrace();
        }


    }
}
