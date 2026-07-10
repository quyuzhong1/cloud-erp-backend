package com.erp.server.tms.handler.asynctask;

import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.LogisticsBillCostAsyncTaskDelegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class LogisticsUpdateReconciliationBatchPushHandler
    implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO> {

    @Lazy
    @Resource
    private LogisticsBillCostAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Override
    public String taskDisplayName() {
        return "对账状态变更异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "对账状态变更异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO payload) {
        if (StringUtils.isBlank(payload.getReconciliationStatus())) {
            return "对账状态不能为空";
        }
        return null;
    }

    @Override
    public List<String> pageBatchIds(String taskId,
                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                     TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO payload,
                                     String lastId,
                                     int batchSize,
                                     TmsAsyncTaskRecordEntity taskRecord) {
        String costType = asyncTaskDelegate.resolveCostTypeFromUpdateReconciliationMethodType(envelope.getMethodType());
        BatchBusinessIdProvider defaultProvider = (cursor, size) -> {
            LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO query =
                asyncTaskDelegate.buildUpdateReconciliationStatusPageQuery(payload, costType);
            query.setLastId(cursor);
            query.setBatchSize(size);
            return logisticsBillCostService.pageByUpdateReconciliationStatus(query);
        };
        return asyncTaskRecordService.pageBatchBusinessIds(
            envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
            defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO payload,
                                                                LoginUser operatorUser,
                                                                List<String> batchIds,
                                                                int batchNumber,
                                                                CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        String costType = asyncTaskDelegate.resolveCostTypeFromUpdateReconciliationMethodType(envelope.getMethodType());
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        return asyncTaskDelegate.processUpdateReconciliationStatusBatch(
            taskId, batchIds, payload.getReconciliationStatus(), payload.getConfirmTime(),
            costType, operatorUser, timeoutSeconds, staleDetailSeconds);
    }
}
