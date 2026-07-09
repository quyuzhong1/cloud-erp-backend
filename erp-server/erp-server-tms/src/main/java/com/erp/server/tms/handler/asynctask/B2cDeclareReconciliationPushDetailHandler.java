package com.erp.server.tms.handler.asynctask;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.service.TmsAsyncTaskBatchDetailPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.TmsB2cDeclareReconciliationAsyncTaskDelegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class B2cDeclareReconciliationPushDetailHandler
    implements TmsAsyncTaskBatchDetailPushHandler<TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO> {

    @Lazy
    @Resource
    private TmsB2cDeclareReconciliationAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Override
    public String taskDisplayName() {
        return "B2C报关对账下推异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "B2C报关对账下推异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO payload) {
        if (payload.getStartDate() == null || payload.getEndDate() == null) {
            return "开始日期或结束日期为空";
        }
        return null;
    }

    @Override
    public String emptyFirstBatchMessage(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                         TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO payload) {
        if (envelope != null
            && TmsAsyncTaskRecordDTO.RETRY_MODE_FAILED_ONLY.equals(envelope.getRetryMode())) {
            return "无失败明细可重试";
        }
        return "b2c报关对账单明细为空";
    }

    @Override
    public String resolveBusinessType(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                      TmsAsyncTaskRecordEntity taskRecord,
                                      TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO payload) {
        String businessType = StringUtils.defaultIfBlank(envelope.getBusinessType(), taskRecord.getBusinessType());
        if (StringUtils.isBlank(businessType)) {
            businessType = SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode();
        }
        return businessType;
    }

    @Override
    public List<TmsAsyncTaskDetailEntity> prepareBatchDetails(String taskId,
                                                              String businessType,
                                                              TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                              TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO payload,
                                                              String cursor,
                                                              int batchSize) {
        return asyncTaskDelegate.prepareDeclareReconciliationBatchDetails(
            taskId, businessType, envelope.getRetryMode(), envelope.getRetrySourceTaskId(),
            payload.getStartDate(), payload.getEndDate(), cursor, batchSize);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processPreparedBatch(String taskId,
                                                                         TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                         TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO payload,
                                                                         List<TmsAsyncTaskDetailEntity> batchDetails,
                                                                         int batchNumber,
                                                                         CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        return asyncTaskDelegate.executeDeclareReconciliationBatch(
            taskId, batchDetails, payload.getStartDate(), payload.getEndDate(), timeoutSeconds, staleDetailSeconds);
    }
}
