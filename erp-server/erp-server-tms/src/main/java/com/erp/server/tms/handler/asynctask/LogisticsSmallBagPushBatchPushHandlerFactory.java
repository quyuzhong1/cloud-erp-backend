package com.erp.server.tms.handler.asynctask;

import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.LogisticsBillCostAsyncTaskDelegate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class LogisticsSmallBagPushBatchPushHandlerFactory {

    @Lazy
    @Resource
    private LogisticsBillCostAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    public TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO> create(
        TmsAsyncTaskRecordEntity mqTaskRecord) {
        return new LogisticsSmallBagPushBatchPushHandler(
            mqTaskRecord, asyncTaskDelegate, asyncTaskRecordService, logisticsBillCostService);
    }
}
