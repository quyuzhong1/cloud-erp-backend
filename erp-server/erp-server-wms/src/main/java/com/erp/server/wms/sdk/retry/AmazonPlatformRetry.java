package com.erp.server.wms.sdk.retry;

import com.common.business.annotation.PlatformRetryAnno;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.server.wms.service.IPlatformRetryService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
@PlatformRetryAnno(method = PlatformDictEnum.AMAZON)
public class AmazonPlatformRetry implements IPlatformRetryService<T> {
    @Resource
    private DmpAmazonFeign dmpMongoDbFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Override
    public Boolean retrySoOutStock(SoB2cEntity currentEntity, List list) {
        if(!currentEntity.hasPlatformWarehouseOrder()){
            return soOutstockService.defaultHandleRetry(currentEntity, list);
        }else{
            return dmpMongoDbFeign.checkAndSendSoOutStock(new DmpPullSoOutStockDTO(currentEntity.getShopId(), currentEntity.getPlatformCode(), currentEntity.getId()));
        }
    }
}
