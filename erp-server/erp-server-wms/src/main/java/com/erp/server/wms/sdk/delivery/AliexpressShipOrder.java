package com.erp.server.wms.sdk.delivery;

import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
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

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {

    }
}
