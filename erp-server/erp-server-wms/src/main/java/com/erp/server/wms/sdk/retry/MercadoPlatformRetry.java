package com.erp.server.wms.sdk.retry;

import com.common.business.annotation.PlatformRetryAnno;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.wms.service.IPlatformRetryService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
@PlatformRetryAnno(method = PlatformDictEnum.MERCADOLIBRE)
public class MercadoPlatformRetry implements IPlatformRetryService {

    @Resource
    private SoOutstockService soOutstockService;
    @Override
    public Boolean retrySoOutStock(SoB2cEntity currentEntity, List list) {
        return soOutstockService.defaultHandleRetry(currentEntity, list);
    }
}
