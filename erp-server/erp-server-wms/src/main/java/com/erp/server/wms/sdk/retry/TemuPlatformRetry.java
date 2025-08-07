package com.erp.server.wms.sdk.retry;

import com.common.business.annotation.PlatformRetryAnno;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.consumer.SyncNewTeMuOutStockConsumer;
import com.erp.server.wms.service.IPlatformRetryService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
@PlatformRetryAnno(method = PlatformDictEnum.TE_MU)
public class TemuPlatformRetry implements IPlatformRetryService<T> {

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private SyncNewTeMuOutStockConsumer teMuOutStockConsumer;

    @Override
    public Boolean retrySoOutStock(SoB2cEntity currentEntity, List list) {
        if(!currentEntity.hasPlatformWarehouseOrder()){
            return soOutstockService.defaultHandleRetry(currentEntity, list);
        }else{
            List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpTaskFeign.getOutputTaskRecord(currentEntity.getPlatformCode(),"DmpOutputTeMuSoOutstockRocketMQTaskHandler");
            if (dmpOutputTaskRecordEntityList == null || dmpOutputTaskRecordEntityList.isEmpty()) {
                log.error("Temu平台重试失败，未找到对应的推送任务记录，订单号: {}", currentEntity.getPlatformCode());
                return false;
            }
            for (DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
                teMuOutStockConsumer.handle(dmpOutputTaskRecordEntity.getRequestData());
            }
        }
        return true;
    }
}
