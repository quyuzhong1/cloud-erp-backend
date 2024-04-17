package com.erp.server.wms.sdk.delivery;

import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.TIK_TOK)
public class TikTokShipOrder implements IPlatformService {

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {

    }
}
