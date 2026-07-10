package com.erp.server.tms.handler.asynctask;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.service.TmsAsyncTaskBatchDetailPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.TmsFirstMileReconciliationAsyncTaskDelegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class FirstMileReconciliationPushDetailHandler
    implements TmsAsyncTaskBatchDetailPushHandler<TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO> {

    @Lazy
    @Resource
    private TmsFirstMileReconciliationAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Override
    public String taskDisplayName() {
        return "头程对账下推异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "头程对账下推异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO payload) {
        if (payload.getStartDate() == null || payload.getEndDate() == null) {
            return "开始日期或结束日期为空";
        }
        return null;
    }

    @Override
    public String emptyFirstBatchMessage(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                         TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO payload) {
        if (envelope != null
            && TmsAsyncTaskRecordDTO.RETRY_MODE_FAILED_ONLY.equals(envelope.getRetryMode())) {
            return "无失败明细可重试";
        }
        return "周期内已签收未对账的物流单为空";
    }

    @Override
    public String resolveBusinessType(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                      TmsAsyncTaskRecordEntity taskRecord,
                                      TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO payload) {
        String businessType = StringUtils.defaultIfBlank(envelope.getBusinessType(), taskRecord.getBusinessType());
        if (StringUtils.isBlank(businessType)) {
            businessType = SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode();
        }
        return businessType;
    }

    @Override
    public List<TmsAsyncTaskDetailEntity> prepareBatchDetails(String taskId,
                                                              String businessType,
                                                              TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                              TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO payload,
                                                              String cursor,
                                                              int batchSize) {
        return asyncTaskDelegate.prepareFirstMileReconciliationBatchDetails(
            taskId, businessType, envelope.getRetryMode(), envelope.getRetrySourceTaskId(),
            payload.getStartDate(), payload.getEndDate(), cursor, batchSize);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processPreparedBatch(String taskId,
                                                                         TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                         TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO payload,
                                                                         List<TmsAsyncTaskDetailEntity> batchDetails,
                                                                         int batchNumber,
                                                                         CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        return asyncTaskDelegate.executeFirstMileReconciliationBatch(
            taskId, batchDetails, payload.getStartDate(), payload.getEndDate(), timeoutSeconds, staleDetailSeconds);
    }
}
