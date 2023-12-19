package com.erp.server.wms.sdk.delivery;

import com.common.business.annotation.PlatformAnnotate;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.server.wms.service.IPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.WALMART)
public class WalmartShipOrder implements IPlatformService<T> {

    @Override
    public String shipOrder(ShopAuthorizeUrlDTO dto) {
        return null;
    }

}
